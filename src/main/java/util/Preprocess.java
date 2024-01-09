package util;

import com.google.common.geometry.*;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class preprocess the data by reading the data from the shapefile, creating the area class and build the neighboring relations
 */
public class Preprocess {


    public static ArrayList<Area> GeoSetBuilder(String dataset) throws IOException {
        ArrayList<Area> areas = new ArrayList<>();
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = preprocess(dataset);
        ArrayList<S2Polyline> polygons = initial_construct(collection, areas, dataset); // change name to polylines
        setNeighbors(polygons, areas);


        return areas;
    }

    private static FeatureCollection<SimpleFeatureType, SimpleFeature> preprocess(String dataset) throws IOException {

        File file = null;
        switch (dataset) {
            case "2k":
                file = new File("DataFile/2056dataset/merged.shp");
                break;
            case "diversity":
                file = new File("DataFile/diversity/2000data.shp");
                break;
            case "island":
                file = new File("DataFile/islanddata/WAandPENN.shp");
                break;
            case "5k":
                file = new File("DataFile/5K/5K.shp");
                break;
            case "10k":
                file = new File("DataFile/10K/10K.shp");
                break;
            case "20k":
                file = new File("DataFile/20K/20K.shp");
                break;
            case "30k":
                file = new File("DataFile/30K/30K.shp");
                break;
            case "40k":
                file = new File("DataFile/40K/40K.shp");
                break;
            case "50k":
                file = new File("DataFile/50K/50K.shp");
                break;
            case "60k":
                file = new File("DataFile/60K/60K.shp");
                break;
            case "70k":
                file = new File("DataFile/70K/70K.shp");
                break;
            case "80k":
                file = new File("DataFile/80K/80K.shp");
                break;
        }
        //System.out.println(file.getTotalSpace());
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;
        dataStore.dispose();
        return source.getFeatures(filter);
    }

    private static ArrayList<S2Polyline> initial_construct(FeatureCollection<SimpleFeatureType, SimpleFeature> collection, ArrayList<Area> areas, String dataset) {
        ArrayList<Geometry> polygons = new ArrayList<>();
        ArrayList<S2Polygon> polygonsS2 = new ArrayList<>(); // testing
        ArrayList<S2Polyline> polylinesS2 = new ArrayList<>(); // testing
        int geo_index = 0;
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                long extensive_attr;
                long internal_attr;

                if (dataset.equals("2k")) {
                    extensive_attr = Long.parseLong((feature.getAttribute("aland").toString()));
                    internal_attr = Long.parseLong(feature.getAttribute("awater").toString());
                } else if (dataset.equals("diversity")) {
                    extensive_attr = (long) Double.parseDouble((feature.getAttribute("cty_pop200").toString()));
                    internal_attr = (long) (1000 * Double.parseDouble((feature.getAttribute("ratio").toString())));

                } else {
                    extensive_attr = Long.parseLong((feature.getAttribute("ALAND").toString()));
                    internal_attr = Long.parseLong(feature.getAttribute("AWATER").toString());
                }
                feature.getAttribute("int");
                Geometry polygon = (Geometry) feature.getDefaultGeometry();
                polygons.add(polygon);
                Coordinate[] coor = polygon.getCoordinates();
                List<S2Point> verticesLoop = new ArrayList<>();
                for (Coordinate coordinate : coor) {
                    verticesLoop.add(S2LatLng.fromDegrees(coordinate.getY(), coordinate.getX()).toPoint()); // is the x lat or lng?
                }

                Coordinate testcoor = new Coordinate(); // testing

                S2Loop outerLoop = new S2Loop(verticesLoop); // testing
                S2Polygon polygonS2 = new S2Polygon(outerLoop); // testing
                polygonsS2.add(polygonS2); // testing
                S2Polyline polylineS2 = new S2Polyline(verticesLoop); // testing
                polylinesS2.add(polylineS2); // testing
                Area newArea = new Area(geo_index, internal_attr, extensive_attr, verticesLoop);
                geo_index++;
                areas.add(newArea);
            }
        }
        // These should match but keeps producing the wrong boolean value
//        for (int i = 0; i < polygonsS2.size(); i++) {
//            for (int j = i + 1; j < polygonsS2.size(); j++) {
//                boolean test1 = polygons.get(i).intersects(polygons.get(j));
//       //         boolean test2 = polygonsS2.get(i).intersects(polygonsS2.get(j));
//                boolean test3 = polylinesS2.get(i).intersects(polylinesS2.get(j));
//                if(test1 ^ test3) {
//                    System.out.println("Failed");
//                }
//            }
//
//        }

        return polylinesS2;

    }

    /*   private static void setNeighbors(ArrayList<S2Polygon> polygons, ArrayList<Area> areas) {

          // ArrayList<Geometry> geoPolygons = new ArrayList<>();
           GeometryFactory geoFactory = new GeometryFactory();

           for (int i = 0; i < polygons.size(); i++) {

               for (int j = i + 1; j < polygons.size(); j++) {
                   ArrayList<Coordinate> coor = new ArrayList<>();
                   for (S2Point point : polygons.get(i).loop(0).vertices()) {
                       S2LatLng coorLatLng = new S2LatLng(point);
                       Coordinate coor1 = new Coordinate(coorLatLng.latDegrees(), coorLatLng.lngDegrees());
                       coor.add(coor1);
                   }
                   Geometry geoPolygon = geoFactory.createPolygon(coor.toArray(new Coordinate[coor.size()]));
                   ArrayList<Coordinate> coor1 = new ArrayList<>();
                   for (S2Point point : polygons.get(j).loop(0).vertices()) {
                       S2LatLng coorLatLng = new S2LatLng(point);
                       Coordinate coor2 = new Coordinate(coorLatLng.latDegrees(), coorLatLng.lngDegrees());
                       coor1.add(coor2);
                   }
                   Geometry geoPolygon2 = geoFactory.createPolygon(coor1.toArray(new Coordinate[coor1.size()]));
                   if (geoPolygon.intersects(geoPolygon2)) {

                       Geometry intersection = geoPolygon.intersection(geoPolygon2);
                       if (intersection.getGeometryType() != "Point") {
                           areas.get(i).add_neighbor(j);
                           areas.get(j).add_neighbor(i);
                       }
                   }
               }
           }
           for (int i = 0; i < polygons.size(); i++) {
               for (int j = i + 1; j < polygons.size(); j++) {
                   if (polygons.get(i).intersects(polygons.get(j))) {
                       S2Polygon intersection = new S2Polygon();
                       intersection.initToIntersection(polygons.get(i), polygons.get(i));
                       final double EPSILON = 1e-10;
                       if (intersection.getArea() > EPSILON) {
                           areas.get(i).add_neighbor(i);
                           areas.get(j).add_neighbor(i);
                       }
                       else {
                           System.out.println("test");
                       }
                   }
               }
               if ((i % 100 == 0) && (!polygons.isEmpty())) {
                   double result = (double) i / polygons.size();
                   System.out.println(result * 100.00 + "%");
               }
           }
       } */
//    private static void setNeighbors(ArrayList<Geometry> polygons, ArrayList<Area> areas) {
//
//        for (int i = 0; i < polygons.size(); i++) {
//
//            for (int j = i + 1; j < polygons.size(); j++) {
//                if (polygons.get(i).intersects(polygons.get(j))) {
//
//                    Geometry intersection = polygons.get(i).intersection(polygons.get(j));
//                    if (intersection.getGeometryType() != "Point") {
//                        areas.get(i).add_neighbor(j);
//                        areas.get(j).add_neighbor(i);
//                    }
//                }
//            }
//        }
//
//
//    }
//}
    private static void setNeighbors(ArrayList<S2Polyline> polylines, ArrayList<Area> areas) {

        for (int i = 0; i < polylines.size(); i++) {
            long startTime = System.nanoTime();
            for (int j = i + 1; j < polylines.size(); j++) {
                if (polylines.get(i).intersects(polylines.get(j))) {
                    areas.get(i).add_neighbor(j);
                    areas.get(j).add_neighbor(i);
                }
            }
            if ((i % 100 == 0) && (!polylines.isEmpty())) {
                double result = (double) i / polylines.size();
                long duration = (System.nanoTime() - startTime) / 1000000;
                System.out.println(result * 100.00 + "%");
                System.out.println("Time: " + duration + "ms");
            }
        }
    }
}