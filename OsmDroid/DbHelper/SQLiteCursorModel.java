 
public class SQLiteCursorModel {

    /**
     * 列集合，key为列名，value为列值
     */
    private Map<String, String> columns = new HashMap<>();

    /**
     * 列集合，key为列名，value为排序
     */
    private Map<String,Integer> indexs=new HashMap<>();

    /**
     * 获取查询结果,key为列名，value为列值
     *
     * @return 结果
     */
    public Map<String, String> getColumns() {
        return columns;
    }

    /**
     * 获取字段的排序
     *
     * @param attrName the attr name
     * @return the int
     */
    public int getIndex(String attrName){

       return indexs.get(attrName);

    }


    /**
     * 获取属性字段 索引
     *
     * @return the indexs
     */
    public Map<String, Integer> getIndexs() {
        return indexs;
    }

    /**
     * SQLite数据库 查询结果指针模型（单个指针）
     *
     * @param cursor            the curso
     */
    public SQLiteCursorModel(Cursor cursor) {

        int removeCount=0;

        for (int i = 0; i < cursor.getColumnCount(); i++) {

            String colName = cursor.getColumnName(i);

            String colValue = "";

            try {
                colValue = cursor.getString(i);
            } catch (Exception e) {
                removeCount=removeCount+1;

                 continue;
            }
            indexs.put(colName,i-removeCount);

            columns.put(colName, colValue);
        }
    }

}
