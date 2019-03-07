
public class FeatureLayerOverlay {

    private FeatureOverlaySet featureOverlaySet;

    private String layerName;

    private String sourceName;

    private boolean isVisible = true;

    private List<Overlay> featureOverlay = new ArrayList<>();

    public FeatureOverlaySet getFeatureOverlaySet() {
        return featureOverlaySet;
    }

    public String getLayerName() {
        return layerName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;

        switch (featureOverlaySet.getGeometryType()) {
            case "MultiLineString":

                for (Overlay line : featureOverlay
                        ) {
                    PolylineOverlay polylineOverlay = (PolylineOverlay) line;

                    polylineOverlay.setVisible(visible);
                }

                break;
            case "MultiPolygon":

                for (Overlay polygon : featureOverlay
                        ) {
                    PolygonOverlay polygonOverlay = (PolygonOverlay) polygon;

                    polygonOverlay.setVisible(visible);
                }

                break;
            default:

                break;

        }
    }

    public List<Overlay> getFeatureOverlay() {
        return featureOverlay;
    }

    public FeatureLayerOverlay(FeatureOverlaySet featureOverlaySet, String layerName, String sourceName) {

        this.featureOverlaySet = featureOverlaySet;

        this.layerName = layerName;

        this.sourceName = sourceName;

        this.featureOverlaySet.setLayer(this);

    }


}
