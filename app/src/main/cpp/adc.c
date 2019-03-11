#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>

#include <sys/ioctl.h>

#include <linux/i2c.h>
#include <linux/i2c-dev.h>


float adc_result[4];

unsigned int fd;

unsigned int ads7828_AP;

unsigned char i2c_buf[64];

void ads7828_Read_ADC_Result(int ch)
{
  unsigned int temp;

  switch (ch) {
    case 0:
      i2c_buf[0] = 0x01;  //AP
      i2c_buf[1] = 0xC5;  //Config MSB
      i2c_buf[2] = 0x83;  //Config LSB
      break;
    case 1:
      i2c_buf[0] = 0x01;  //AP
      i2c_buf[1] = 0xD5;  //Config MSB
      i2c_buf[2] = 0x83;  //Config LSB
      break;
    case 2:
      i2c_buf[0] = 0x01;  //AP
      i2c_buf[1] = 0xE5;  //Config MSB
      i2c_buf[2] = 0x83;  //Config LSB
      break;
    case 3:
      i2c_buf[0] = 0x01;  //AP
      i2c_buf[1] = 0xF5;  //Config MSB
      i2c_buf[2] = 0x83;  //Config LSB
      break;
    default:
      break;
    }

  if(write(fd, &i2c_buf, 3) < 0) printf("write i2c error!!!\n");
  //else  printf("write i2c ok!!!\n");

  usleep(1000);       //Wait for ADC convert finish

  ads7828_AP = 0x00;

  if(write(fd, &ads7828_AP, 1) < 0) printf("write i2c error!!!\n");
  //else  printf("write i2c ok!!!\n");

  if(read(fd, i2c_buf, 2) != 2) printf("read i2c error!!!\n");
  //else printf("read i2c ok!!!\n");

  temp = (i2c_buf[0] << 8) + i2c_buf[1];

  temp = temp >> 4;

  adc_result[ch] = ((float)temp / 2048) * 2.048;     //12bit including 1 bit sign;

  printf("i2c read ADC CH[%d]:%f", ch, adc_result[ch]);
  printf("\n");
}


int main()
{
  unsigned int ads1015_addr = 0x49; //0x48

  unsigned int i;

  printf("\n\n\n");

  fd = open("/dev/i2c-0", O_RDWR);

  if(!fd)
    {
      printf("error to open i2c!!!\n");
      exit(1);
    }

  printf("open i2c success!!!\n");

  if(ioctl(fd, I2C_TENBIT, 0) < 0)printf("set i2c address error!!!\n");   //7 bit address;

  //check i2c device works fine or not;
  if(ioctl(fd, I2C_SLAVE_FORCE, ads1015_addr) < 0)
    {
      printf("i2c slave address check failed!!!\n");
      close(fd);
    }
  else
    {
      printf("i2c slave address check ok!!!\n");
    }

  if(ioctl(fd, I2C_TIMEOUT, 1) < 0) printf("set i2c timeout error!!!\n");
  if(ioctl(fd, I2C_RETRIES, 3) < 0) printf("set i2c retry error!!!\n");

  printf("\n\n");

  for(i=0;i<4;i++)
  {
    ads7828_Read_ADC_Result(i);
  }

  close(fd);

  return 0;
}

