package cn.sp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("cn.sp.dao")//MyBatis 支持
@SpringBootApplication
public class PageInterceptorDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PageInterceptorDemoApplication.class, args);
	}

}
