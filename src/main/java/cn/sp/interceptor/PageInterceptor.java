package cn.sp.interceptor;

import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 利用拦截器实现分页
 * Created by 2YSP on 2019/7/7.
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
        RowBounds.class, ResultHandler.class})
})
@Slf4j
public class PageInterceptor implements Interceptor {

  /**
   * Executor.query()方法中,MappedStatement对象在参数列表中的索引位置
   */
  private static int MAPPEDSTATEMENT_INDEX = 0;

  /**
   * 用户传入的实参对象在参数列表中的索引位置
   */
  private static int PARAMTEROBJECT_INDEX = 1;
  /**
   * 分页对象在参数列表中的索引位置
   */
  private static int ROWBOUNDS_INDEX = 2;


  /**
   * 执行拦截逻辑的方法
   */
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    // 参数列表
    Object[] args = invocation.getArgs();
    final MappedStatement mappedStatement = (MappedStatement) args[MAPPEDSTATEMENT_INDEX];
    final Object parameter = args[PARAMTEROBJECT_INDEX];
    final RowBounds rowBounds = (RowBounds) args[ROWBOUNDS_INDEX];
    // 获取offset,即查询的起始位置
    int offset = rowBounds.getOffset();
    int limit = rowBounds.getLimit();
    // 获取BoundSql对象，其中记录了包含"?"占位符的SQL语句
    final BoundSql boundSql = mappedStatement.getBoundSql(parameter);
    // 获取BoundSql中记录的SQL语句
    String sql = boundSql.getSql();
    log.info("==========sql:\n" + sql);
    sql = getPagingSql(sql, offset, limit);
    // 重置RowBounds对象
    args[ROWBOUNDS_INDEX] = new RowBounds(RowBounds.NO_ROW_OFFSET, RowBounds.NO_ROW_LIMIT);
    // 根据当前语句创建新的MappedStatement
    args[MAPPEDSTATEMENT_INDEX] = createMappedStatement(mappedStatement, boundSql, sql);
    // 通过Invocation.proceed()方法调用被拦截的Executor.query()方法
    return invocation.proceed();
  }

  private Object createMappedStatement(MappedStatement mappedStatement, BoundSql boundSql,
      String sql) {
    // 创建新的BoundSql对象
    BoundSql newBoundSql = createBoundSql(mappedStatement, boundSql, sql);
    Builder builder = new Builder(mappedStatement.getConfiguration(), mappedStatement.getId(),
        new BoundSqlSqlSource(newBoundSql), mappedStatement.getSqlCommandType());
    builder.useCache(mappedStatement.isUseCache());
    builder.cache(mappedStatement.getCache());
    builder.databaseId(mappedStatement.getDatabaseId());
    builder.fetchSize(mappedStatement.getFetchSize());
    builder.flushCacheRequired(mappedStatement.isFlushCacheRequired());

    builder.keyColumn(delimitedArrayToString(mappedStatement.getKeyColumns()));
    builder.keyGenerator(mappedStatement.getKeyGenerator());
    builder.keyProperty(delimitedArrayToString(mappedStatement.getKeyProperties()));

    builder.lang(mappedStatement.getLang());
    builder.resource(mappedStatement.getResource());

    builder.parameterMap(mappedStatement.getParameterMap());
    builder.resultMaps(mappedStatement.getResultMaps());
    builder.resultOrdered(mappedStatement.isResultOrdered());
    builder.resultSets(delimitedArrayToString(mappedStatement.getResultSets()));
    builder.resultSetType(mappedStatement.getResultSetType());

    builder.timeout(mappedStatement.getTimeout());
    builder.statementType(mappedStatement.getStatementType());

    return builder.build();

  }

  public String delimitedArrayToString(String[] array) {
    String result = "";
    if (array == null || array.length == 0) {
      return result;
    }
    for (int i = 0; i < array.length; i++) {
      result += array[i];
      if (i != array.length - 1) {
        result += ",";
      }
    }
    return result;
  }

  class BoundSqlSqlSource implements SqlSource {

    private BoundSql boundSql;

    public BoundSqlSqlSource(BoundSql boundSql) {
      this.boundSql = boundSql;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
      return boundSql;
    }
  }

  private BoundSql createBoundSql(MappedStatement mappedStatement, BoundSql boundSql, String sql) {
    BoundSql newBoundSql = new BoundSql(mappedStatement.getConfiguration(), sql,
        boundSql.getParameterMappings(), boundSql.getParameterObject());
    return newBoundSql;
  }

  /**
   * 重写sql
   */
  private String getPagingSql(String sql, int offset, int limit) {
    sql = sql.trim();
    boolean hasForUpdate = false;
    String forUpdatePart = "for update";
    if (sql.toLowerCase().endsWith(forUpdatePart)) {
      // 将当前SQL语句的"for update片段删除"
      sql = sql.substring(0, sql.length() - forUpdatePart.length());
      hasForUpdate = true;
    }

    StringBuilder result = new StringBuilder();
    result.append(sql);
    result.append(" limit ");
    result.append(offset);
    result.append(",");
    result.append(limit);

    if (hasForUpdate) {
      result.append(" " + forUpdatePart);
    }
    return result.toString();
  }

  /**
   * 决定是否触发intercept()方法
   */
  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  /**
   * 根据配置初始化Interceptor对象
   */
  @Override
  public void setProperties(Properties properties) {
    log.info("properties: " + properties.getProperty("testProp"));
  }
}
