package cn.sp.bean;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class Person implements Serializable {

  private Integer id;

  private String name;

  private String sex;

  private Integer age;

  private Date birthday;


}
