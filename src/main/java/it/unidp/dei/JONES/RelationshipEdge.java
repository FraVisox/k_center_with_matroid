package it.unidp.dei.JONES;

import it.unidp.dei.Point;

import org.jgrapht.graph.DefaultWeightedEdge;

//DefaultWeightedEdge with a Point as a label
class RelationshipEdge
        extends DefaultWeightedEdge
{
    public RelationshipEdge()
    {
        labelPoint = null;
    }
    public RelationshipEdge(Point p)
    {
        labelPoint = p;
    }
    public Point getLabelPoint() {
        return labelPoint;
    }
    private final Point labelPoint;

}
