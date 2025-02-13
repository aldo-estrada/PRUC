package util;

import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import org.locationtech.jts.geom.Coordinate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class describes the Area class which is a spatial area that has numerical attribute and represented by a set of marginal coordinates
 */
public class Area implements Cloneable , Serializable {

    private int index;
    private long sim_attr;
    private long extensive_attr;
    private List<S2Point> coor_array;
    private S2Point centroid;
    private ArrayList<Integer> neigh_area_index;
    private int associate_region_index;

    /**
     *
     * @param index the unique identifier of an area
     * @param sim_attr the similarity attribute
     * @param extensive_attr the extensive attribute
     * @param coor_array the set of coordinates that marks the margin of this area
     */
    public Area(int index , long sim_attr, long extensive_attr , List<S2Point> coor_array)
    {
        this.index = index;
        this.sim_attr = sim_attr;
        this.extensive_attr = extensive_attr;
        this.coor_array = coor_array; // changed
        neigh_area_index = new ArrayList<>();
        associate_region_index = -1;
    }



    public void set_centroid()
    {
        double total_x = 0.0;
        double total_y = 0.0;
        for (S2Point coordinate : coor_array) {
            S2LatLng coorLatLng = new S2LatLng(coordinate);
            total_x += coorLatLng.latDegrees();
            total_y += coorLatLng.lngDegrees();
        }
        double ave_x = total_x / coor_array.size();
        double ave_y = total_y / coor_array.size();
        S2LatLng latLngCentroid = S2LatLng.fromDegrees(ave_x, ave_y);

        centroid = new S2Point(latLngCentroid.toPoint().getX(), latLngCentroid.toPoint().getY(), latLngCentroid.toPoint().getZ());
        S2LatLng coorLatLng = new S2LatLng(centroid);
        double lat = coorLatLng.latDegrees();
        double lng = coorLatLng.lngDegrees();
    }

    /**
     *
     * @param a the area to compute distance with
     * @return the euclidean distance between this area and area a
     */
    public double compute_dist(Area a)
    {
      //  S2Point a_centroid = a.get_centroid();
        S2LatLng a_centroid = new S2LatLng(a.get_centroid());
        S2LatLng LatLngCentroid = new S2LatLng(centroid);
//        double lng1 = LatLngCentroid.lngDegrees();
//        double lat1 = LatLngCentroid.latDegrees();
//        double lng2 = a_centroid.lngDegrees();
//        double lat2 =  a_centroid.latDegrees();
//        double value1 = Math.sqrt((LatLngCentroid.lngDegrees() - a_centroid.lngDegrees()) * (LatLngCentroid.lngDegrees() - a_centroid.lngDegrees()) + (LatLngCentroid.latDegrees() - a_centroid.latDegrees()) * (LatLngCentroid.latDegrees() - a_centroid.latDegrees()));
        return  Math.sqrt((LatLngCentroid.lngDegrees() - a_centroid.lngDegrees()) * (LatLngCentroid.lngDegrees() - a_centroid.lngDegrees()) + (LatLngCentroid.latDegrees() - a_centroid.latDegrees()) * (LatLngCentroid.latDegrees() - a_centroid.latDegrees()));
              //Math.sqrt((centroid.getX() - a.get_centroid().getX()) * (centroid.getX() - a.get_centroid().getX()) + (centroid.getY() - a_centroid.getY()) * (centroid.getY() - a_centroid.getY()));
    }

    public void set_centroid(S2Point centroid)
    {
        this.centroid = centroid;
    }

    public void set_region(int region_index)
    {
        this.associate_region_index = region_index;
    }

    public void add_neighbor(int add_index)
    {
        neigh_area_index.add(add_index);
    }

    public void set_neighbor_once(ArrayList<Integer> neighbor_to_set)
    {
        this.neigh_area_index = neighbor_to_set;
    }

    public int get_geo_index() { return index; }

    public long get_internal_attr()
    {
        return sim_attr;
    }

    public long get_extensive_attr()
    {
        return extensive_attr;
    }


    public ArrayList<Area> get_neigh_area(ArrayList<Area> all_areas) {
        ArrayList<Area> neigh_areas = new ArrayList<>();
        for(int neigh_index : neigh_area_index)
        {
            neigh_areas.add(all_areas.get(neigh_index));
        }
        return neigh_areas;
    }

    public ArrayList<Integer> get_neigh_area_index()
    {
        return neigh_area_index;
    }

    public int get_associated_region_index() { return associate_region_index; }

    public List<S2Point> get_coordinates() { return coor_array; }

    public S2Point get_centroid() { return centroid; }


    public long compute_hetero(Area neigh_area) {
        return Math.abs(sim_attr - neigh_area.get_internal_attr());
    }

    public void initialize_neighbor() {
        neigh_area_index = new ArrayList<>();
    }



    @Override
    protected Object clone() {
        Area g = new Area(this.get_geo_index() , this.get_internal_attr() , this.get_extensive_attr() , this.get_coordinates());
        g.set_region(this.get_associated_region_index());
        g.set_neighbor_once((ArrayList<Integer>)neigh_area_index.clone());
        g.set_centroid(this.get_centroid());
        return g;
    }


    public static ArrayList<Area> area_list_copy(ArrayList<Area> all_areas) throws CloneNotSupportedException {
        ArrayList<Area> returned_areas = new ArrayList<>();
        for(Area g : all_areas)
        {
            returned_areas.add((Area)g.clone());
        }
        return returned_areas;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Area)) return false;
        return this.get_geo_index() == ((Area) o).get_geo_index();
    }





}

