import java.util.Comparator;
import java.util.TreeSet;

public class Router {
    private TreeSet<Route> routes;

    private static Comparator<Route> routeComparator = (Route r1, Route r2) -> {
        int result = Integer.compare(r2.getNetwork().getMaskLength(), r1.getNetwork().getMaskLength());

        if ( result == 0 ) {
            result = Integer.compare(r1.getMetric(), r2.getMetric());
        }
        return result;
    };

    public Router(Iterable<Route> routes) {
        this.routes = new TreeSet<>(routeComparator);

        for (Route route : routes) {
            this.routes.add(route);
        }
    }

    public void addRoute(Route route) {
        this.routes.add(route);
    }

    public Route getRouteForAddress(IPv4Address address) {
        for ( Route route: this.routes ) {
            if ( route.getNetwork().contains(address) ) {
                return route;
            }
        }
        return null;
    }

    public Iterable<Route> getRoutes() {
        return this.routes;
    }

    public void removeRoute(Route route) {
        this.routes.remove(route);
    }
}
