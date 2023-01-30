package ass1;
import java.util.List;

public class RouteInfo {
    List<INode> route;
    boolean isRouted;

    public RouteInfo (List<INode> route, boolean isRouted) {
        this.route    = route;
        this.isRouted = isRouted;
    }

    public boolean isRouted() {
        return isRouted;
    }

    public List<INode> getRoute() {
        return route;
    }
}
