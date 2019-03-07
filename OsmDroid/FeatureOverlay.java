 
public class FeatureOverlay {

    //region 字段
    /**
     * 列集合，key为列名，value为列值
     */
    private Map<String, String> attributes = new HashMap<>();

    /**
     * 所在集合
     */
    private FeatureOverlaySet parentSet;

    /**
     * 空间属性
     */
    private Geometry geometry;
    //endregion

    //region 属性
    /**
     * 获取属性值
     *
     * @return 结果
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * 获取父集合
     *
     * @return the parent set
     */
    public FeatureOverlaySet getParentSet() {
        return parentSet;
    }

    /**
     * 获取空间属性
     *
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }
    //endregion

    public FeatureOverlay(Map<String,String> attributes,Geometry geometry,FeatureOverlaySet parentSet){

        this.attributes=attributes;

        this.geometry=geometry;

        this.parentSet=parentSet;
    }

    /**
     * 通过SQLite查询指针创建要素对象 .
     *
     * @param cursorModel the cursor model
     * @return the feature overlay
     * @throws ParseException the parse exception
     */
    public static FeatureOverlay createFromCursorModel(SQLiteCursorModel cursorModel,FeatureOverlaySet parentSet) throws ParseException {
        //获取wkt
        String wkt=cursorModel.getColumns().get(MyApplication.getGeomField());

        WKTReader wktReader=new WKTReader();

        Geometry temp=wktReader.read(wkt);

        return new FeatureOverlay(cursorModel.getColumns(),temp,parentSet);
    }
}
