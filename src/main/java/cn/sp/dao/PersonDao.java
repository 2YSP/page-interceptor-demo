package cn.sp.dao;


import cn.sp.bean.Person;
import java.util.List;
import org.apache.ibatis.session.RowBounds;

public interface PersonDao {

  void save(Person person);

  Person queryById(Integer id);

  List<Person> queryPersonsByPage(RowBounds rowBounds);
}
