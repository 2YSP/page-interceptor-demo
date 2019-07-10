package cn.sp.dao;


import cn.sp.bean.Person;
import java.util.List;
import org.apache.ibatis.session.RowBounds;

public interface PersonDao {

  List<Person> queryPersonsByPage(RowBounds rowBounds);
}
