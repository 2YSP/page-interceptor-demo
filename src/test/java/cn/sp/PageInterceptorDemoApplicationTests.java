package cn.sp;

import cn.sp.bean.Person;
import cn.sp.dao.PersonDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.ibatis.session.RowBounds;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
@RunWith(SpringRunner.class)
@SpringBootTest
public class PageInterceptorDemoApplicationTests {

	@Autowired
	private PersonDao personDao;

	ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void contextLoads() throws JsonProcessingException {
		int offset = 2;
		int limit = 2;
		List<Person> list = personDao.queryPersonsByPage(new RowBounds(offset, limit));
		System.out.println(objectMapper.writer().writeValueAsString(list));
	}


}
