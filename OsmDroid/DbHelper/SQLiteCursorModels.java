 
public class SQLiteCursorModels   {


    //region 字段

    /**
     * 数据集合（行）
     */
    private ArrayList<SQLiteCursorModel> entrys;

    //endregion


    //region 属性获取

    /**
     * 获取数据
     *
     * @return the entrys
     */
    public ArrayList<SQLiteCursorModel> getEntrys() {
        return entrys;
    }

    //endregion

    //region 构造函数

    public SQLiteCursorModels(ArrayList<SQLiteCursorModel> entrys){
        this.entrys=entrys;
    }

    public SQLiteCursorModels(){
        entrys=new ArrayList<>();
    }

    //endregion

    /**
     * 添加数据结果（行）
     *
     * @param model the model
     */
    public void add(SQLiteCursorModel model){

        entrys.add(model);

    }

}
