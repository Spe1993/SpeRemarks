 
public class FeatureOverlaySet {


    private List<FeatureOverlay> featureOverlays;

    private String geometryType;

    private FeatureLayerOverlay layer;

    public FeatureLayerOverlay getLayer() {
        return layer;
    }

    public void setLayer(FeatureLayerOverlay layer) {
        this.layer = layer;
    }

    public List<FeatureOverlay> getFeatureOverlays() {
        return featureOverlays;
    }

    public String getGeometryType() {
        return geometryType;
    }

    public static FeatureOverlaySet createFrom(List<FeatureOverlay> featureOverlays){

        FeatureOverlaySet temp=new FeatureOverlaySet();

        temp.featureOverlays=featureOverlays;


        return temp;
    }

    public static FeatureOverlaySet createFrom(SQLiteCursorModels sqLiteCursorModels){
        FeatureOverlaySet temp=new FeatureOverlaySet();


        List<FeatureOverlay> tempList=new ArrayList<>();

        for (SQLiteCursorModel entry:sqLiteCursorModels.getEntrys()
             ) {
            try {
                tempList.add( FeatureOverlay.createFromCursorModel(entry,temp));


            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        temp.featureOverlays=tempList;

       temp.geometryType=  tempList.get(0).getGeometry().getGeometryType();

        return temp;
    }
}
