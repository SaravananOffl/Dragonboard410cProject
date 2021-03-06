/*
 * linux/drivers/misc/et4007/et4007.c
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 */
#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/init.h>
#include <linux/poll.h>
#include <linux/fb.h>
#include <linux/slab.h>
#include <linux/miscdevice.h>
#include <linux/delay.h>
#include <linux/timer.h>
#include <linux/jiffies.h>
#include <linux/platform_device.h>
#include <linux/irq.h>
#include <asm/irq.h>
#include <asm/io.h>
#include <linux/gpio.h>
#include <linux/of_gpio.h>
#include "et4007.h"

#define DEVICE_NAME		"irremote"
#define DRIVER_NAME		"et4007"

#define START_DELAY     200
#define STOP_DELAY      200
#define CLK_DELAY       50

static struct ir_remocon_data *ir_data = NULL;
//#define ET_POWERCONTROL

#define		ET4007_ADDRESS					0x35
#define		ET4007_CONTROL_SEND_CODE_1		0x53
#define		ET4007_CONTROL_SEND_CODE_2		0x55
#define		ET4007_CONTROL_SEND_CODE_3		0x54

#define		ET4007_CONTROL_START_LEARND		0x57
#define		ET4007_CONTROL_READ_VERSION		0x5c
#define		ET4007_CONTROL_READ_CODE		0x5d
#define		ET4007_CONTROL_STOP_LEARN		0x5e
#define		ET4007_CONTROL_SEND_REPEAT		0x5f
#define     ET_CMD_START_LEARN 				0
#define     ET_CMD_STOP_LEARN 				1
#define     ET_CMD_REPEAT					2
#define     ET_CMD_VERSION					3

#define ET4007_SCL_GPIO			    ir_data->clk_gpio
#define ET4007_SDA_GPIO			    ir_data->sda_gpio
#define ET4007_BUSY_GPIO		  	ir_data->busy_gpio

#define ET4007_SET_SCL_OUTPUT()		gpio_direction_output(ET4007_SCL_GPIO,1)
#define ET4007_SET_SDA_INPUT()	  	gpio_direction_input(ET4007_SDA_GPIO)
#define ET4007_SET_SDA_OUTPUT()		gpio_direction_output(ET4007_SDA_GPIO,1)
#define ET4007_SET_BUSY_INPUT()	  	gpio_direction_input(ET4007_BUSY_GPIO)

#define ET4007_GET_SDA_STATE()		gpio_get_value(ET4007_SDA_GPIO)
#define ET4007_GET_BUSY_STATE()		gpio_get_value(ET4007_BUSY_GPIO)

#define ET4007_SET_SCL_LOW()		gpio_set_value(ET4007_SCL_GPIO, 0)
#define ET4007_SET_SCL_HIGH()		gpio_set_value(ET4007_SCL_GPIO, 1)
#define ET4007_SET_SDA_LOW()		gpio_set_value(ET4007_SDA_GPIO, 0)
#define ET4007_SET_SDA_HIGH()		gpio_set_value(ET4007_SDA_GPIO, 1)

static DECLARE_WAIT_QUEUE_HEAD(remote_waitq);

static volatile int busy_status = 0;
struct timer_list remote_timer;
struct class *et4007_class = NULL;

/**
 * Funcation: et_et4007_xcal_crc
 * Input:  uint8_t *ptr	uint32_t len
 * Output: uint8_t crc
 * Desc: get whole ptr data array crc
 */
static uint8_t et4007_xcal_crc(uint8_t *ptr, uint32_t len)
{
	uint8_t crc;
	uint8_t i;
	crc = 0;
	while(len--) {
		crc ^= *ptr++;
		for (i = 0; i < 8; i++) {
			if(crc & 0x01) {
				crc = (crc >> 1) ^ 0x8C;
			} else {
				crc >>= 1;
			}
		}
	}
	return crc;
}

/**
 * Funcation: et_et4007_compare_time
 * Input:  	emote_data data, uint16_t high_level
 * uint16_t low_level
 * Output: 	true or false
 * Desc: compare  remote data time
 */
static char et4007_compare_time(struct remote_data data, uint16_t high_level,uint16_t low_level)
{
	if (((data.high_level - high_level) < 2) &&((data.high_level - high_level ) > -2)
		&&((data.low_level - low_level) < 2) && ((data.low_level - low_level) > -2))
	{
		return 1;
	} else {
		return 0;
	}
	return 0;
}

/**
 * Funcation: et4007_compare_alldata
 * Input: emote_data data, uint16_t *sample int index
 * Output: true or false
 * Desc: compare  remote data to all sample
 */
static int et4007_compare_alldata(struct remote_data rmt_data, uint16_t *sample, int index)
{
	int i;
	uint16_t timeHigh, timeLow;

	for (i = 0; i < index; i += 2) {
		timeHigh = sample[i];
		timeLow = sample[i + 1];
		if (et4007_compare_time(rmt_data, timeHigh, timeLow)) {
			return 1; // rmt_data is equal sample data
		}
	}
	return 0; // rmt_data is not equal sample data
}

static void et4007_push_sample_time_data(struct remote_data data, uint16_t *sample, int index)
{
	sample[index] = data.high_level;
	sample[index + 1] = data.low_level;
}

static int et4007_sample_time_selection(struct ir_remocon_data *ir_data)
{
	int i, index;
	struct remote_data rmt_data;
	index = 0;

	for (i = 0; i < ir_data->count; i += 2) {
		rmt_data.high_level = ir_data->original[i];
		rmt_data.low_level = ir_data->original[i + 1];

		if (index != 0) {
			if (et4007_compare_alldata(rmt_data, ir_data->sample, index) == 0) {
				et4007_push_sample_time_data(rmt_data,  ir_data->sample, index);
				index += 2;
				if (index > MAX_SAMPLE_INDEX){
					index = MAX_SAMPLE_INDEX;
					return -1;
				}
			}
		} else { /* first data send*/
			et4007_push_sample_time_data(rmt_data, ir_data->sample, index);
			index += 2;
		}
	}
	ir_data->index = index;
	return index;
}

/**
 * Funcation: et4007_get_index
 * Input:  	ir_remocon_data *ir_data
 * uint16_t *sample, int index
 * Output: 	index
 * Desc:	data compare sample to get sample index
 */
int et4007_get_index(struct remote_data data, uint16_t *sample, int index)
{
	int i = 0;
	uint16_t timeHigh, timeLow;
	for (i = 0; i < index; i += 2) {
		timeHigh = sample[i];
		timeLow = sample[i+1];
		if (et4007_compare_time(data, timeHigh, timeLow)) {
			return i;
		}
	}
	return 32;
}

/**
 * Funcation: et4007_get_data_index
 * Input: ir_remocon_data *ir_data, char *data,
 * uint16_t *sample, int index
 * Output: index
 * Desc: original data to get sample index to compress data
 */
static int et4007_get_data_index(struct ir_remocon_data *ir_data)
{
	int i, j = 0, count = 0;
	char temp;
	struct remote_data rmt_data;

	for (i = 0; i < ir_data->count; i += 2) {
		rmt_data.high_level = ir_data->original[i];
		rmt_data.low_level = ir_data->original[i+1];

		temp = et4007_get_index(rmt_data, ir_data->sample, ir_data->index);
		if (temp > 32) {
			return -1;
		}
		ir_data->data[count++] = (temp / 2) ;
	}

	ir_data->couple = count;
	i = 0;
	j = 0;
	while (i < count) {
		temp = (ir_data->data[i++] << 4) & 0xf0;
		temp |= (ir_data->data[i++]) & 0x0f;
		ir_data->data[j++] = temp;

		if (j > MAX_DATA) {
			j = MAX_DATA;
			return -1;
		}
	}

	ir_data->data_count = j;

	return j;
}

/**
 * Funcation: et4007_depress_sample
 * Input: uint16_t *in int index
 * Output: char *out
 * Desc: change uint16_t sample to double char sample
 */
static int et4007_depress_sample(struct ir_remocon_data *ir_data)
{
	int i, j = 0;
	if (ir_data->index>MAX_SAMPLE_INDEX){
		ir_data->index = MAX_SAMPLE_INDEX;
		return -1;
	}

	for (i = 0; i < ir_data->index; i++) {
		ir_data->zp_sample[j++] = (uint8_t)(ir_data->sample[i] >> 8) & 0xff;
		ir_data->zp_sample[j++] = (uint8_t)ir_data->sample[i];
	}
	ir_data->index = j;
	return j;
}

/**
 * Funcation: et4007_compress_original_data
 * Input: ir_remocon_data *ir_data
 * Output: ir_remocon_data *ir_data	ir_data length
 * Desc: translate original consumer data to ET compress data
 */
static int et4007_compress_original_data(struct ir_remocon_data *ir_data)
{
	uint8_t temp[MAX_SEND_DATA];
	int i;
	int err;

	memset(temp, 0x00, MAX_DATA);
	memset(ir_data->data, 0x00, MAX_DATA*2);
	memset(ir_data->sample, 0x00, MAX_INDEX);
	memset(ir_data->zp_sample, 0x00, MAX_INDEX * 2);
	err = et4007_sample_time_selection(ir_data);
	if (err < 0) {
		pr_err("%s: et4007_sample_time_selection program error\n", __func__);
		return err;
	}
	err = et4007_get_data_index(ir_data);
	if (err < 0) {
		pr_err("%s: et4007_get_data_index program error\n", __func__);
		return err;
	}

	err = et4007_depress_sample(ir_data);
	if (err < 0) {
		pr_err("%s: et4007_depress_sample program error\n", __func__);
		return err;
	}

	ir_data->length= MAX_INDEX + ir_data->data_count + 10;

	for (i = 0; i < ir_data->index; i++) {
		temp[i] = ir_data->zp_sample[i];
	}
	for (i = 0; i < ir_data->data_count; i++) {
		temp[i  + MAX_INDEX] = ir_data->data[i];
		//printk("temp[%d] is 0x%x",i,ir_data->data[i]);
	}

	ir_data->signal[0] = ET4007_CONTROL_SEND_CODE_3;
	ir_data->signal[1] = (ir_data->length>>8)&0xff;
	ir_data->signal[2] = ir_data->length&0xff;
	ir_data->signal[3] = ir_data->freq;
	ir_data->signal[4] = (ir_data->couple>>8)&0xff;
	ir_data->signal[5] = (ir_data->couple)&0xff;;   //reserve
	ir_data->signal[6] = 0x00;	 //reserve
	ir_data->signal[7] = 0x00;
	ir_data->signal[8] = 0x01;
	ir_data->signal[9] = et4007_xcal_crc(temp,ir_data->length - 10);

	for (i = 0; i < MAX_INDEX + ir_data->data_count; i++) {
		ir_data ->signal[i+10]= temp[i];
	}
#if 0
	for (i = 0; i < ir_data->length; i++) {
		printk("remoteData[%d] ----> 0x%02x \n", i, ir_data->signal[i]);
	}
	printk(KERN_INFO  "remote couple is %d \n", ir_data->couple);
	printk(KERN_INFO  "remote freq is %d \n", ir_data->freq);
	printk(KERN_INFO  "remote length is %d \n", ir_data->length);
	printk(KERN_INFO  "remote index is %d \n", ir_data->index);
	printk(KERN_INFO  "remote data_count is %d \n", ir_data->data_count);
	printk(KERN_INFO  "remote count is %d \n", ir_data->count);
	for (i = 0; i < MAX_INDEX; i++) {
		printk("sample [%d] ----> 0x%02x \n", i, ir_data->zp_sample[i]);
	}
#endif
	return ir_data->length;
}

/**
 * Funcation: ET4007 TIMER HANDLE
 * Input:  	unsigned long data
 * Output:
 * Desc: get busy state according to time base
 */
static void remote_timer_handle(unsigned long data)
{
	if (!busy_status) {
		mod_timer(&remote_timer,jiffies+msecs_to_jiffies(100));
		if (!ET4007_GET_BUSY_STATE()) {
			busy_status = 1;
			wake_up_interruptible(&remote_waitq);
		}
	}
}

/**
 * Funcation :test gpio output level
 */
void et4007_start(void)
{
	ET4007_SET_SDA_OUTPUT();
	ET4007_SET_SDA_HIGH();
	ET4007_SET_SCL_OUTPUT();
	ET4007_SET_SCL_HIGH();
	ET4007_SET_SDA_LOW();
	udelay(START_DELAY);
	udelay(START_DELAY);
	udelay(START_DELAY);
	ET4007_SET_SCL_LOW();
	udelay(START_DELAY);
	udelay(START_DELAY);
}

void et4007_stop(void)
{
	udelay(STOP_DELAY);
	ET4007_SET_SDA_LOW();
	ET4007_SET_SDA_OUTPUT();
	udelay(STOP_DELAY);
	ET4007_SET_SCL_HIGH();
	udelay(STOP_DELAY);
	ET4007_SET_SDA_HIGH();
	udelay(STOP_DELAY);
	//ET4007_SET_SDA_INPUT();
	udelay(STOP_DELAY);
}

int et4007_i2c_write_byte(uint8_t dat)
{
	int err = 0;
	uint8_t i, dat_temp;

	dat_temp = dat;
	ET4007_SET_SDA_OUTPUT();
	for (i = 0; i < 8; i++) {
		udelay(10);
		if (dat_temp & 0x80) {
			ET4007_SET_SDA_HIGH();
		} else {
			ET4007_SET_SDA_LOW();
		}
		udelay(CLK_DELAY);
		ET4007_SET_SCL_HIGH();
		dat_temp <<= 1;
		udelay(CLK_DELAY);  // optional
		ET4007_SET_SCL_LOW();
		udelay(CLK_DELAY);
	}
	ET4007_SET_SDA_HIGH();
	ET4007_SET_SDA_INPUT();
	udelay(CLK_DELAY);
	ET4007_SET_SCL_HIGH();
	udelay(CLK_DELAY);
	//if(ET4007_GET_SDA_STATE())err=1;
	//else err=0;
	ET4007_SET_SCL_LOW();
	udelay(CLK_DELAY);

	return err;
}

uint8_t et4007_i2c_read_byte(void)
{
	uint8_t dat, i;

	ET4007_SET_SDA_HIGH();
	ET4007_SET_SDA_INPUT();
	dat = 0;
	for (i = 0; i != 8; i++) {
		udelay(CLK_DELAY);
		ET4007_SET_SCL_HIGH();
		udelay(CLK_DELAY);
		dat <<= 1;
		if(ET4007_GET_SDA_STATE()) dat++;
		ET4007_SET_SCL_LOW();
		udelay(10);
	}

	ET4007_SET_SDA_OUTPUT();
	ET4007_SET_SDA_LOW();
	udelay(CLK_DELAY);
	ET4007_SET_SCL_HIGH();
	udelay(CLK_DELAY);
	ET4007_SET_SCL_LOW();
	udelay(CLK_DELAY);

	return dat;
}

/**
 * Funcation: read ET4007yd18 version code
 * Input:
 * Output: version
 */
static int et4007_read_device_info(void)
{
	int ret = 0;
	int i;
	char buf[8] = {0};
	pr_info("%s: +\n", __func__);

	et4007_start();

	/* write addr and cmd */
	et4007_i2c_write_byte(ET4007_ADDRESS);
	et4007_i2c_write_byte(ET4007_CONTROL_READ_VERSION);

	/* read device info */
	for (i = 0; i < 4; i++) {
		buf[i] = et4007_i2c_read_byte();
	}

	et4007_stop();

	pr_info("%s: dev_id: 0x%02x, 0x%02x, 0x%02x, 0x%02x \n", __func__,
			buf[0], buf[1], buf[2], buf[3]);

	ret = buf[0]*0x1000000 + buf[1]*0x10000 + buf[2]*0x100 + buf[3];

	pr_info("%s: -\n", __func__);
	return ret;
}

static long et4007_ioctl(struct file *filp, unsigned int cmd,unsigned long arg)
{
	int i;
	char buf[4];

	switch(cmd) {
		case ET_CMD_START_LEARN:
			et4007_start();
			et4007_i2c_write_byte(ET4007_ADDRESS);
			et4007_i2c_write_byte(ET4007_CONTROL_START_LEARND);
			et4007_stop();
			busy_status = 0;
			break;
		case ET_CMD_STOP_LEARN:
			et4007_start();
			et4007_i2c_write_byte(ET4007_ADDRESS);
			et4007_i2c_write_byte(ET4007_CONTROL_STOP_LEARN);
			et4007_stop();
			//mod_timer(&remote_timer, 0);
			busy_status = 1;
			printk("ET4007 wake up interruptiable remote watiq\n");
			wake_up_interruptible(&remote_waitq);
			break;
		case ET_CMD_REPEAT:
			et4007_start();
			et4007_i2c_write_byte(ET4007_ADDRESS);
			et4007_i2c_write_byte(ET4007_CONTROL_SEND_REPEAT);
			et4007_stop();
			break;
		case ET_CMD_VERSION:
			et4007_start();
			et4007_i2c_write_byte(ET4007_ADDRESS);
			et4007_i2c_write_byte(ET4007_CONTROL_READ_VERSION);
			for (i = 0; i < 4; i++) {
				buf[i] = et4007_i2c_read_byte();
			}
			et4007_stop();
			return (buf[0]<<16)|(buf[1]<<8)|(buf[2]);
			break;
		default:
			return -EINVAL;
	}

	return 0;
}

static long et4007_ioctl_compat(struct file *filp, unsigned int cmd,unsigned long arg)
{
	int i;
	char buf[4];

	switch(cmd) {
		case ET_CMD_START_LEARN:
			et4007_start();
			et4007_i2c_write_byte(ET4007_ADDRESS);
			et4007_i2c_write_byte(ET4007_CONTROL_START_LEARND);
			et4007_stop();
			busy_status =0;
			break;
		case ET_CMD_STOP_LEARN:
			et4007_start();
			et4007_i2c_write_byte(ET4007_ADDRESS);
			et4007_i2c_write_byte(ET4007_CONTROL_STOP_LEARN);
			et4007_stop();
			//mod_timer(&remote_timer, 0);
			busy_status = 1;
			printk("ET4007 wake up interruptiable remote watiq \n");
			wake_up_interruptible(&remote_waitq);
			break;
		case ET_CMD_REPEAT:
			et4007_start();
			et4007_i2c_write_byte(ET4007_ADDRESS);
			et4007_i2c_write_byte(ET4007_CONTROL_SEND_REPEAT);
			et4007_stop();
			break;
		case ET_CMD_VERSION:
			et4007_start();
			et4007_i2c_write_byte(ET4007_ADDRESS);
			et4007_i2c_write_byte(ET4007_CONTROL_READ_VERSION);
			for (i = 0; i < 4; i++) {
				buf[i]=et4007_i2c_read_byte();
			}
			et4007_stop();
			return (buf[0]<<16)|(buf[1]<<8)|(buf[2]);
			break;
		default:
			return -EINVAL;
	}

	return 0;
}

static unsigned int et4007_poll( struct file *file,struct poll_table_struct *wait)
{
	unsigned int mask = 0;
	mod_timer(&remote_timer, jiffies + msecs_to_jiffies(40));
	poll_wait(file, &remote_waitq, wait);
	if (busy_status) {
		busy_status = 0;
		mask |= POLLIN | POLLRDNORM;
	}
	return mask;
}

static ssize_t et4007_write(struct file *file, const char *buffer, size_t count, loff_t *ppos)
{
	char send_buff[256];
	int ret;
	int i;

	if (count == 0) {
		return -1;
	}
	if(buffer[0]==ET4007_CONTROL_SEND_CODE_3) {
		if (count > 384) {
			printk("ET4007---count error---\n");
			return -1;
		}
		et4007_start();
		et4007_i2c_write_byte(ET4007_ADDRESS);
		for (i = 0; i < count; i++) {
			et4007_i2c_write_byte(buffer[i]);
		}
		et4007_stop();
		return count;
	} else if (buffer[0] == ET4007_CONTROL_SEND_CODE_1) {
		if (count> 129) {
			count = 129;
			printk("ET4007---size error---\n");
			return count;
		}
		memset(send_buff,0x00,129);
		ret = copy_from_user(send_buff, buffer, count) ;

		if (ret) {
			printk("ET4007---copy error---\n");
			return ret;
		}
		et4007_start();
		et4007_i2c_write_byte(ET4007_ADDRESS);
		for (i = 0; i < 129; i++) {
			et4007_i2c_write_byte(send_buff[i]);
		}
		et4007_stop();
		return count;
	} else {
		printk("ET4007---command  error---\n");
		return -1;
	}
	return count;
}

static ssize_t et4007_read(struct file *filp, char *buff,size_t count, loff_t *ppos)
{
	int i, err;
	int ET_crc;
	int ET_PartIndexCount;
	int ET_Sample;
	int ET_Index;
	int ET_Freq;
	int m = 0;
	int max = 0;
	char buf_ir_test[384];

	pr_info("%s: +\n", __func__);

	et4007_start();
	et4007_i2c_write_byte(ET4007_ADDRESS);
	et4007_i2c_write_byte(ET4007_CONTROL_READ_CODE);
	ET_crc = et4007_i2c_read_byte();
	buf_ir_test[m++] = ET_crc;
	ET_PartIndexCount = et4007_i2c_read_byte();
	buf_ir_test[m++] = ET_PartIndexCount;
	ET_Sample = et4007_i2c_read_byte();
	buf_ir_test[m++] = ET_Sample;
	ET_Index = et4007_i2c_read_byte();
	buf_ir_test[m++] = ET_Index;
	ET_Freq = et4007_i2c_read_byte();
	buf_ir_test[m++] = ET_Freq;
	max = ET_PartIndexCount+ET_Sample+ET_Index;
	if (max > 380) {
		et4007_stop();
		return  -EFAULT;
	}

	for (i = 0; i < max; i++) {
		buf_ir_test[m] = et4007_i2c_read_byte();
		m++;
	}

	et4007_stop();

	if (m > sizeof(buf_ir_test)) {
		m = sizeof(buf_ir_test);
	}

	err = copy_to_user((void *)buff, (const void *)(&buf_ir_test[0]), m);

	pr_info("%s: -\n", __func__);

	return err ? -EFAULT : m;
}

static int et4007_open(struct inode *inode, struct file *file)
{
	et4007_read_device_info();
	setup_timer(&remote_timer, remote_timer_handle, (unsigned long)"remote");
	return 0;
}

static int et4007_close(struct inode *inode, struct file *file)
{
	del_timer_sync(&remote_timer);
	return 0;
}

static struct file_operations et4007_fops = {
	.owner			= THIS_MODULE,
	.open			= et4007_open,
	.release		= et4007_close,
	.write          = et4007_write,
	.read			= et4007_read,
	.poll			= et4007_poll,
	.unlocked_ioctl	= et4007_ioctl,
#ifdef CONFIG_COMPAT
	.compat_ioctl   = et4007_ioctl_compat,
#endif
};

static void et4007_remocon_work(struct ir_remocon_data *ir_data)
{
	struct ir_remocon_data *data = ir_data;
	int sleep_timing;
	int emission_time;
	int i;

	//mutex_lock(&data->mutex);

	et4007_start();
	et4007_i2c_write_byte(ET4007_ADDRESS);
	for (i = 0; i < ir_data->length; i++) {
		et4007_i2c_write_byte(ir_data->signal[i]);
	}
	et4007_stop();

	//mutex_unlock(&data->mutex);

	emission_time = ( (data->ir_sum) * (data->freq) * 4 / 5000);
	sleep_timing = emission_time -10;
	if (sleep_timing < 0) {
		sleep_timing = 10;
	}

	data->freq = 0;
	data->ir_sum = 0;
}

static ssize_t et4007_ir_send_store(struct device *dev, struct device_attribute *attr,
		const char *buf, size_t size)
{
	unsigned int data;
	int err;
	int count = 0, i;

	ir_data->count = 0;
	ir_data->send_flag = 1;

	for (i = 0; i < size; i++) {
		if (sscanf(buf++, "%u", &data) == 1) {
			if (data == 0 || buf == '\0') {
				break;
			}
			if (count == 0) {
				ir_data->orig_freq = data;
				if (ir_data->orig_freq < 10000||ir_data->orig_freq > 100000) {
					printk("freq is error\n\r");
					return -1;
				}
				ir_data->freq = (char)(1200000/data + 1) ;
				count++;
			} else {
				ir_data->ir_sum  += data;
				ir_data->original[ir_data->count++] = (uint16_t)data ;
				count++;
				if(ir_data->count > 1024){
					printk("count > 1024 error \n\r");
					return -1;
				}
			}
			while (data > 0) {
				buf++;
				data /= 10;
			}
		} else {
			break;
		}
	}

	if (ir_data->count < 5) {
		printk("data->count < 5 error \n\r");
		return -1;
	}

	err = et4007_compress_original_data(ir_data);
	if (err < 0) {
		printk("et4007_compress_original_data  error \n\r");
		return -1;
	}

	et4007_remocon_work(ir_data);

	return size;
}

static ssize_t et4007_ir_send_show(struct device *dev, struct device_attribute *attr, char *buf)
{
	int i;
	int len = 0;
	char buf_read[8] = {0};

	pr_info("%s: +\n", __func__);

	et4007_start();

	/* write addr and cmd */
	et4007_i2c_write_byte(ET4007_ADDRESS);
	et4007_i2c_write_byte(ET4007_CONTROL_READ_VERSION);

	/* read device info */
	for (i = 0; i < 4; i++) {
		buf_read[i] = et4007_i2c_read_byte();
	}

	et4007_stop();

	for (i = 0; i < 4; i++) {
		if (i > 0)
			len += sprintf(buf + len, ",");

		len += sprintf(buf + len, "0x%02x", buf_read[i]);
	}
	len += sprintf(buf + len, "\n");

	pr_info("%s: -\n", __func__);

	return len;
}

static DEVICE_ATTR(ir_send, 0664, et4007_ir_send_show, et4007_ir_send_store);

static ssize_t et4007_ir_learn_store(struct device *dev, struct device_attribute *attr,
		const char *buf, size_t size)
{
	unsigned int data;

	if (sscanf(buf++, "%u", &data) == 1) {
		if (data == 1) {
			et4007_start();
			et4007_i2c_write_byte(ET4007_ADDRESS);
			et4007_i2c_write_byte(ET4007_CONTROL_START_LEARND);
			et4007_stop();
			pr_info("%s: start remote learn \n", __func__);
		} else if (data == 0) {
			et4007_start();
			et4007_i2c_write_byte(ET4007_ADDRESS);
			et4007_i2c_write_byte(ET4007_CONTROL_STOP_LEARN);
			et4007_stop();
			pr_info("%s: stop remote learn \n", __func__);
		}
	}
	return size;
}

static ssize_t et4007_ir_learn_show(struct device *dev, struct device_attribute *attr, char *buf)
{
	int crc, part_index_count, sample, index, freq;
	int i, m = 0, max = 0, len = 0;
	char buf_read[386] = {0};

	et4007_start();
	et4007_i2c_write_byte(ET4007_ADDRESS);
	et4007_i2c_write_byte(ET4007_CONTROL_READ_CODE);

	/* crc */
	crc = et4007_i2c_read_byte();
	buf_read[m++] = crc;

	/* part index count */
	part_index_count = et4007_i2c_read_byte();
	buf_read[m++] = part_index_count;

	/* sample */
	sample = et4007_i2c_read_byte();
	buf_read[m++] = sample;

	/* index */
	index = et4007_i2c_read_byte();
	buf_read[m++] = index;

	/* freq */
	freq = et4007_i2c_read_byte();
	buf_read[m++] = freq;

	pr_info("%s: part_index_count = %d, sample = %d, index = %d\n", __func__,
			part_index_count, sample, index);

	max = part_index_count + sample + index;
	if (max > 380) {
		et4007_stop();
		sprintf(buf, "error");
		return len;
	}

	for (i = 0; i < max; i++) {
		buf_read[m] = et4007_i2c_read_byte();
		m++;
	}

	et4007_stop();

	for (i = 0; i < m; i++) {
		if (i == 0) {
			len = sprintf(buf, "%d", buf_read[i]);
		} else {
			len += sprintf(buf + len, ",%d", buf_read[i]);
		}
		//pr_info("%s: learn data[%d] = %d\n", __func__, i, buf_read[i]);
	}
	pr_info("\n");
	len += sprintf(buf + len, "\n");
	return	len;
}
static DEVICE_ATTR(ir_learn, 0664, et4007_ir_learn_show, et4007_ir_learn_store);

static ssize_t et4007_ir_state_show(struct device *dev, struct device_attribute *attr, char *buf)
{
	int len = 0;
	if (ET4007_GET_BUSY_STATE()) {
		len = sprintf(buf + len, "%d\n", 1);
	} else {
		len = sprintf(buf + len, "%d\n", 0);
	}
	return	len;
}
static DEVICE_ATTR(ir_state, 0444, et4007_ir_state_show, NULL);

static ssize_t et4007_ir_info_show(struct device *dev, struct device_attribute *attr, char *buf)
{
	int i, len = 0;
	len += sprintf(buf + len, "%d,", ir_data->send_flag);
	len += sprintf(buf + len, "%d,", ir_data->orig_freq);
	for (i = 0; i < ir_data->count; i++) {
		len += sprintf(buf + len, "%d,", ir_data->original[i]);
	}

	len += sprintf(buf + len, "\n");
	ir_data->send_flag = 0;
	return	len;
}
static DEVICE_ATTR(ir_info, 0444, et4007_ir_info_show, NULL);

static int et4007_parse_dt(struct device *dev)
{
	if (!dev || !dev->of_node || !ir_data)
		return -1;

	ir_data->clk_gpio = of_get_named_gpio_flags(dev->of_node, "etek,clk-gpio", 0, NULL);
	if (!gpio_is_valid(ir_data->clk_gpio)) {
		pr_err("%s: clk gpio is invalid !\n ", __func__);
		return ir_data->clk_gpio;
	}

	pr_info("%s: clk gpio = %d\n", __func__, ir_data->clk_gpio);

	ir_data->sda_gpio = of_get_named_gpio_flags(dev->of_node, "etek,sda-gpio", 0, NULL);
	if (!gpio_is_valid(ir_data->sda_gpio)) {
		pr_err("%s: sda gpio is invalid\n ", __func__);
		return ir_data->sda_gpio;
	}

	pr_info("%s: sda gpio = %d\n", __func__, ir_data->sda_gpio);

	ir_data->busy_gpio = of_get_named_gpio_flags(dev->of_node, "etek,busy-gpio", 0, NULL);
	if (!gpio_is_valid(ir_data->busy_gpio)) {
		pr_err("%s: busy gpio is invalid\n ", __func__);
		return ir_data->busy_gpio;
	}

	pr_info("%s: busy gpio = %d\n", __func__, ir_data->busy_gpio);

	return 0;
}

static struct miscdevice et4007_misc_dev = {
	.minor = MISC_DYNAMIC_MINOR,
	.name = DEVICE_NAME,
	.fops = &et4007_fops,
};

static int et4007_probe(struct platform_device *pdev)
{
	struct device *ir_remocon_dev;
	int error;
	pr_info("%s: +\n", __func__);
#ifdef ET_POWERCONTROL
	ET4007_SET_POWER_HIGH();
	mdelay(1);
#endif
	ir_data = kzalloc(sizeof(struct ir_remocon_data), GFP_KERNEL);
	if (NULL == ir_data) {
		pr_err("%s: failed to kzalloc memory for ir_data !\n", __func__);
		error = -ENOMEM;
		goto err_free_mem;
	}

	error = et4007_parse_dt(&pdev->dev);
	if (error) {
		pr_err("%s: failed to parse dt !\n", __func__);
		return error;
	}

	error = gpio_request(ET4007_SCL_GPIO, DEVICE_NAME);
	if (error) {
		pr_err("%s: failed to request scl gpio (%d) !\n", __func__, ET4007_SCL_GPIO);
		return error;
	}

	error = gpio_request(ET4007_SDA_GPIO, DEVICE_NAME);
	if (error) {
		pr_err("%s: failed to request sda gpio (%d) !\n", __func__, ET4007_SDA_GPIO);
		return error;
	}

	error = gpio_request(ET4007_BUSY_GPIO, DEVICE_NAME);
	if (error) {
		pr_err("%s: failed to request busy gpio (%d) !\n", __func__, ET4007_BUSY_GPIO);
		return error;
	}

	et4007_class = class_create(THIS_MODULE, "etek");
	if (IS_ERR(et4007_class)) {
		pr_err("%s: failed to create class(etek)!\n", __func__);
		return PTR_ERR(et4007_class);
	}

	ir_remocon_dev = device_create(et4007_class, NULL, 0, ir_data, "sec_ir");

	if (IS_ERR(ir_remocon_dev))
		pr_err("%s: failed to create ir_remocon_dev device\n", __func__);

	if (device_create_file(ir_remocon_dev, &dev_attr_ir_send) < 0)
		pr_err("%s: failed to create device file(%s)!\n", __func__,
				dev_attr_ir_send.attr.name);

	if (device_create_file(ir_remocon_dev, &dev_attr_ir_state) < 0)
		pr_err("%s: failed to create device file(%s)!\n", __func__,
				dev_attr_ir_state.attr.name);

	if (device_create_file(ir_remocon_dev, &dev_attr_ir_learn) < 0)
		pr_err("%s: failed to create device file(%s)!\n", __func__,
				dev_attr_ir_learn.attr.name);

	if (device_create_file(ir_remocon_dev, &dev_attr_ir_info) < 0)
		pr_err("%s: failed to create device file(%s)!\n", __func__,
				dev_attr_ir_info.attr.name);

	ET4007_SET_SCL_OUTPUT();
	ET4007_SET_SDA_OUTPUT();
	ET4007_SET_BUSY_INPUT();
	ET4007_SET_SCL_HIGH();
	ET4007_SET_SDA_HIGH();

	et4007_read_device_info();

	pr_info("%s: -\n", __func__);

	return error;
err_free_mem:
#ifdef ET_POWERCONTROL
	ET4007_SET_POWER_LOW();
#endif
	kfree(ir_data);
	ir_data = NULL;
	return error;
}

static int et4007_remove(struct platform_device *pdev)
{
	if (et4007_class)
		device_destroy(et4007_class, 0);

	if (ir_data)
		kfree(ir_data);

	return 0;
}

static struct of_device_id et4007_match_table[] = {
	{.compatible = "etek,ir", },
	{ },
};

static struct platform_driver et4007_driver = {
	.probe    = et4007_probe,
	.remove   = et4007_remove,
	// .suspend  = et4007_suspend,
	//  .resume   = et4007_resume,
	.driver   = {
		.owner   = THIS_MODULE,
		.name    = DEVICE_NAME,
		.of_match_table = et4007_match_table,
	},
};

static int __init et4007_init(void)
{
	if(misc_register(&et4007_misc_dev)) {
		pr_err("%s: misc_register register failed\n", __func__);
	}
	return platform_driver_register(&et4007_driver);
}

static void __exit et4007_exit(void)
{
	misc_deregister(&et4007_misc_dev);
	platform_driver_unregister(&et4007_driver);
}

module_init(et4007_init);
module_exit(et4007_exit);

MODULE_LICENSE("GPL");
MODULE_AUTHOR("ETEK Inc.");
MODULE_DESCRIPTION("ETEK consumerir Driver");


