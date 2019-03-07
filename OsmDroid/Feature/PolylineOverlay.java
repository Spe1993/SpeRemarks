 
public class PolylineOverlay extends Polyline {

    private Geometry geometry;



    private Map<String,String> attributes;

    private FeatureOverlay featureOverlay;

    public PolylineOverlay(FeatureOverlay featureOverlay){

        geometry=featureOverlay.getGeometry();

        attributes=featureOverlay.getAttributes();

        this.featureOverlay=featureOverlay;

        List<GeoPoint> tempPoint=OsmToJts.convertToGeoPointList(geometry.getCoordinates());

        setPoints(tempPoint);

    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {


        Projection pj = mapView.getProjection();
        GeoPoint eventPos = (GeoPoint)pj.fromPixels((int)event.getX(), (int)event.getY());
        double tolerance = (double)(this.getPaint().getStrokeWidth() * mapView.getContext().getResources().getDisplayMetrics().density);
        GeoPoint closest = this.getCloseTo(eventPos, tolerance, mapView);
        if (closest != null) {

            AttributeList attributeList = new AttributeList(MyApplication.getInstance().getMainActivity());

            String[] firstTab = QueryConfigModel.GetFirstTabAttr(featureOverlay.getParentSet().getLayer().getSourceName());

            for (int i = 0; i < firstTab.length; i++) {
                attributeList.InsertTextText(String.format("%s    ", firstTab[i]), attributes.get(firstTab[i]));
            }

            BubbleLinearLayout bubbleLinearLayout =   ViewStyleInitialize.initBubbleLinearLayout();

            bubbleLinearLayout.addView(attributeList);

            InfoWindow infoWindow = new InfoWindow(bubbleLinearLayout, mapView) {
                @Override
                public void onOpen(Object o) {

                }

                @Override
                public void onClose() {

                }
            };

            setInfoWindow(infoWindow);



            this.onClickDefault(this, mapView, closest);

            return true;
        } else {
            InfoWindow.closeAllInfoWindowsOn(mapView);

            return false;
        }
    }

    @Override
    public boolean onDoubleTap(MotionEvent e, MapView mapView) {
        Projection pj = mapView.getProjection();
        GeoPoint eventPos = (GeoPoint)pj.fromPixels((int)e.getX(), (int)e.getY());
        double tolerance = (double)(this.getPaint().getStrokeWidth() * mapView.getContext().getResources().getDisplayMetrics().density);
        GeoPoint closest = this.getCloseTo(eventPos, tolerance, mapView);
        if (closest != null) {

            AttributeList attributeList = new AttributeList(MyApplication.getInstance().getMainActivity());

            Map<String,String>  tempAttrs=attributes;

            tempAttrs.remove(MyApplication.getGeomField());

            for (Map.Entry<String,String> entry:tempAttrs.entrySet()
                 ) {
                attributeList.InsertTextText(String.format("%s    ", entry.getKey()),entry.getValue());
            }


            BubbleLinearLayout bubbleLinearLayout =   ViewStyleInitialize.initBubbleLinearLayout();

            bubbleLinearLayout.addView(attributeList);

            InfoWindow infoWindow = new InfoWindow(bubbleLinearLayout, mapView) {
                @Override
                public void onOpen(Object o) {

                }

                @Override
                public void onClose() {

                }
            };

            setInfoWindow(infoWindow);

            this.onClickDefault(this, mapView, closest);



            return true;
        } else {

            InfoWindow.closeAllInfoWindowsOn(mapView);

            return false;
        }
    }

    public boolean onClickDefault(Polyline polyline, MapView mapView, GeoPoint eventPos) {

        InfoWindow.closeAllInfoWindowsOn(mapView);

        polyline.setInfoWindowLocation(eventPos);

        polyline.showInfoWindow();

        mapView.getController().animateTo(eventPos,17d,1500l);

        return true;
    }


}
