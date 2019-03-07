 
public class SpatialiteHelper {
    /**
     * 数据库对象
     */
    private SQLiteDatabase sqLiteDatabase;

    public SpatialiteHelper(String path) {

        sqLiteDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);

    }


    //region 普通查询（不涉及空间数据）

    /**
     * 普通查询（对比GeomQuery更快）
     *
     * @param sql 查询语句
     * @return the array list
     */
    public SQLiteCursorModels Query(String sql) {
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);

        SQLiteCursorModels arrayList = new SQLiteCursorModels();

        if (cursor != null && cursor.moveToFirst()) {

            arrayList = ConvertCursorToList(cursor);

        }
        return arrayList;
    }


    /**
     * 根据列名统计
     *
     * @param tableName 表明
     * @param colName   列名
     * @param lengthCol 长度列名
     * @return the sq lite cursor models
     */
    public SQLiteCursorModels StaticByCol(String tableName, String colName, String lengthCol) {

        SQLiteCursorModels result = new SQLiteCursorModels();

        SQLiteCursorModels distinctModels = Distinct(tableName, colName);

        for (SQLiteCursorModel model : distinctModels.getEntrys()
                ) {
            String sql = String.format("select %s, sum(%s) as %s ,count() as %s from %s where %s='%s'", colName, lengthCol, lengthCol, "数目", tableName, colName, model.getColumns().values().toArray()[0]);

            SQLiteCursorModels tempStaticItem = Query(sql);

            result.add(tempStaticItem.getEntrys().get(0));
        }

        return result;

    }


    public SQLiteCursorModels Distinct(String tableName, String column) {

        String sql = String.format("select distinct %s from %s", column, tableName);

//        SQLiteCursorModels dists=Query(sql);

        return Query(sql);

    }

    /**
     * 将重置位置的游标传入，将会自动遍历游标到最后，并转换为序列
     *
     * @param cursor
     * @return
     */
    protected SQLiteCursorModels ConvertCursorToList(Cursor cursor) {

        SQLiteCursorModels result = new SQLiteCursorModels();

        while (!cursor.isAfterLast()) {

            SQLiteCursorModel tempModel = new SQLiteCursorModel(cursor);

            result.add(tempModel);

            cursor.moveToNext();
        }

        return result;
    }
    //endregion


    //region 空间查询（涉及空间数据--需要将geom字段进行wkt转换--非空间查询建议不用此类查询方法，会降低效率）


    public  boolean run(String sql){
        try{
            sqLiteDatabase.execSQL(sql);
            return  true;
        }catch (Exception e){

            return  false;
        }



    }

    public boolean deleteGeom(String tableName, String wkt, String geometry) {

        try {
            String sql = String.format("Delete  FROM %s WHERE ST_astext(%s)='%s'", tableName, geometry, wkt);

            sqLiteDatabase.execSQL(sql);
            return true;
        } catch (Exception e) {

            return false;
        }


    }


    /**
     * m模糊查询--关键字查询
     *
     * @param tableName 表名
     * @param keyWord   关键字
     * @return the sq lite cursor models
     */
    public SQLiteCursorModels fuzzyQuery(String tableName, String keyWord) {

        String cols = String.format("PRAGMA  table_info([%s])", tableName);

        SQLiteCursorModels colsInfo = Query(cols);

//        String sql=String.format("select * from %s where ",tableName);

        String sql = String.format(" Select a.*,ASTEXT(b.geom) as geometry from %s a , %s b where a.id=b.id and(", tableName, tableName);

        for (SQLiteCursorModel model : colsInfo.getEntrys()
                ) {
            String colName = model.getColumns().get("name");

            sql = String.format("%s a.%s like '%s%s%s' or", sql, colName, "%",keyWord,"%");

        }

        sql = sql.substring(0, sql.length() - 2);

        sql = sql + ")";
        return Query(sql);

    }

    /**
     * 对指定表名的表格进行空间查询
     *
     * @param tableName 表名
     * @return the array list
     */
    public SQLiteCursorModels GeomQueryByTableName(String tableName) {

        String sql = String.format("Select a.*,ASTEXT(b.geom) as geometry from %s a , %s b where a.id=b.id", tableName, tableName);

        return GeomQuery(sql);
    }

    /**
     * 获取指定表格的指定字段(自动添加geom字段）
     *
     * @param tableName 表名
     * @param fields    字段
     * @return the array list
     */
    public SQLiteCursorModels GeomQueryFiledsByTableName(String tableName, String[] fields) {

        String sql = "SELECT ";

        for (int i = 0; i < fields.length; i++) {

            sql = String.format("%s %s,", new Object[]{sql, fields[i]});

        }

        sql = String.format("%s ASTEXT(%s) as geometry from %s ", new Object[]{sql, "geom", tableName});

        return GeomQuery(sql);
    }

    /**
     * 空间查询
     *
     * @param tableName 表名
     * @param wkt       the wkt
     * @param srid      the srid
     * @param fields    字段集合
     * @return the array list
     */
    public SQLiteCursorModels QueryIntersectsByGemetry(String tableName, String wkt, int srid, String[] fields) {

        String sql = "SELECT ";

        for (int i = 0; i < fields.length; i++) {

            sql = String.format("%s %s,", new Object[]{sql, fields[i]});

        }

        String createGeometry = String.format(" ST_GeomFromText('%s',%s)", wkt, srid);

        sql = String.format("%s ASTEXT(%s) as geometry from %s where ST_Intersects(%s,%s) = 1 ", new Object[]{sql, "geom", tableName, "geom", createGeometry});

        return GeomQuery(sql);

    }

    /**
     * 获取指定表格的指定字段(自动添加geom字段）
     *
     * @param tableName    表名
     * @param field        字段
     * @param isClearEmpty 是否清除空值结果
     * @return the array list
     */
    public SQLiteCursorModels GeomQueryFiledsByTableName(String tableName, String field, boolean isClearEmpty) {

        String sql = String.format("SELECT  %s ,  ASTEXT(%s) as geometry from %s", field, "geom", tableName);

        if (isClearEmpty) {
            sql = String.format("%s where %s<>''", sql, field, field);

        }

        return GeomQuery(sql);
    }

    /**
     * 单属性查询（适用于属性值类型为文本）
     *
     * @param tableName 表名
     * @param field     字段名称
     * @param value     属性
     * @return the array list
     */
    public SQLiteCursorModels GeomQueryBySingleAttribute(String tableName, String field, String value) {

        String sql = String.format("Select a.*,ASTEXT(b.geom) as geometry from %s a , %s b Where a.%s='%s' and a.id=b.id", tableName, tableName, field, value);

        return GeomQuery(sql);
    }

    /**
     * 单属性查询（适用于属性值类型为整形）
     *
     * @param tableName 表名
     * @param field     字段名称
     * @param value     属性
     * @return the array list
     */
    public SQLiteCursorModels GeomQueryBySingleAttribute(String tableName, String field, int value) {

        String sql = String.format("Select a.*,ASTEXT(b.geom) as geometry from %s a , %s b Where %s=%s", tableName, tableName, field, String.valueOf(value));

        return GeomQuery(sql);
    }

    /**
     * 单属性查询（适用于属性值类型为双精度）
     *
     * @param tableName 表名
     * @param field     字段名称
     * @param value     属性
     * @return the array list
     */
    public SQLiteCursorModels GeomQueryBySingleAttribute(String tableName, String field, double value) {

        String sql = String.format("Select a.*,ASTEXT(b.geom) as geometry from %s a , %s b Where %s=%s", tableName, tableName, field, String.valueOf(value));

        return GeomQuery(sql);
    }


    /**
     * 单属性查询（适用于属性值类型为单精度）
     *
     * @param tableName 表名
     * @param field     字段名称
     * @param value     属性
     * @return the array list
     */
    public SQLiteCursorModels GeomQueryBySingleAttribute(String tableName, String field, float value) {

        String sql = String.format("Select a.*,ASTEXT(b.geom) as geometry from %s a , %s b Where %s=%s", tableName, tableName, field, String.valueOf(value));

        return GeomQuery(sql);
    }

    /**
     * 查询相交的要素Query intersects by gemetry array list.
     *
     * @param tableName 表格/图层名称
     * @param wkt       目标图形的wkt
     * @param srid      坐标系代码
     * @return the array list
     */
    public SQLiteCursorModels QueryIntersectsByGemetry(String tableName, String wkt, int srid) {

        String createGeometry = String.format(" ST_GeomFromText('%s',%s)", wkt, srid);

        String sql = String.format("Select a.*,ASTEXT(b.geom) as geometry from %s a , %s b Where ST_Intersects(a.geom,%s) = 1 and ST_Intersects(b.geom,%s) = 1 ", tableName, tableName, createGeometry, createGeometry);

        return GeomQuery(sql);

    }

    public SQLiteCursorModels GeomQueryIntersectAndContain(String tableName, String wkt, int srid) {
        String createGeometry = String.format(" ST_GeomFromText('%s',%s)", wkt, srid);

        String sql = String.format("Select a.*,ASTEXT(b.geom) as geometry from %s a , %s b Where a.id=b.id and( ST_Intersects(a.geom,%s) = 1 or  ST_Contains(%s,a.geom)=1)", tableName, tableName, createGeometry, createGeometry, createGeometry);

        return GeomQuery(sql);

    }

    /**
     * 空间查询
     *
     * @param sql 查询语句
     * @return the array list
     */
    private SQLiteCursorModels GeomQuery(String sql) {
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);

        SQLiteCursorModels arrayList = new SQLiteCursorModels();

        if (cursor != null && cursor.moveToFirst()) {

            arrayList = ConvertGeomCursorToList(cursor);

        }
        return arrayList;
    }

    /**
     * 将重置位置的游标传入，将会自动遍历游标到最后，并转换为序列
     *
     * @param cursor
     * @return
     */
    protected SQLiteCursorModels ConvertGeomCursorToList(Cursor cursor) {

        SQLiteCursorModels result = new SQLiteCursorModels();

        while (!cursor.isAfterLast()) {

            SQLiteCursorModel tempModel = new SQLiteCursorModel(cursor);

            result.add(tempModel);

            cursor.moveToNext();
        }

        return result;
    }
    //endregion

    /**
     * 关闭数据库连接
     */
    public void CloseConnection() {
        sqLiteDatabase.close();
    }


}
