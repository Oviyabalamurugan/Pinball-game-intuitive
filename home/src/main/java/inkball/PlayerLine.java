package inkball;

import processing.core.PVector;
import java.util.ArrayList;
import java.util.List;

public class PlayerLine {
    private List<PVector> points;

    public PlayerLine(int x, int y) {
        points = new ArrayList<>();
        points.add(new PVector(x, y));
    }

    public void addPoint(int x, int y) {
        PVector lastPoint = points.get(points.size() - 1);
        PVector newPoint = new PVector(x, y);
        if (PVector.dist(lastPoint, newPoint) >= 5) {
            points.add(newPoint);
        }
    }

    public boolean collidesWith(Ball ball) {
        PVector ballPos = ball.getPosition();
        PVector ballNextPos = PVector.add(ballPos, ball.getVelocity());
        float ballRadius = ball.getRadius();

        for (int i = 0; i < points.size() - 1; i++) {
            PVector p1 = points.get(i);
            PVector p2 = points.get(i + 1);

            if (lineSegmentCollision(ballNextPos, ballRadius, p1, p2)) {
                return true;
            }
        }
        return false;
    }

    private boolean lineSegmentCollision(PVector ballNextPos, float ballRadius, PVector p1, PVector p2) {
        float distanceToLine = distancePointToSegment(ballNextPos, p1, p2);
        return distanceToLine <= ballRadius;
    }

    public PVector getNormal(Ball ball) {
        PVector ballPos = ball.getPosition();
        PVector ballNextPos = PVector.add(ballPos, ball.getVelocity());

        for (int i = 0; i < points.size() - 1; i++) {
            PVector p1 = points.get(i);
            PVector p2 = points.get(i + 1);

            if (lineSegmentCollision(ballNextPos, ball.getRadius(), p1, p2)) {
                PVector lineVector = PVector.sub(p2, p1);
                PVector normal1 = new PVector(-lineVector.y, lineVector.x);
                PVector normal2 = new PVector(lineVector.y, -lineVector.x);
                
                normal1.normalize();
                normal2.normalize();

                PVector midpoint = PVector.add(p1, p2).div(2);
                PVector midpoint1 = PVector.add(midpoint, normal1);
                PVector midpoint2 = PVector.add(midpoint, normal2);

                if (PVector.dist(ballPos, midpoint1) < PVector.dist(ballPos, midpoint2)) {
                    return normal1;
                } else {
                    return normal2;
                }
            }
        }

        return new PVector(0, 1); // Default normal if no collision
    }

    private float distancePointToSegment(PVector p, PVector v, PVector w) {
        float l2 = PVector.dist(v, w) * PVector.dist(v, w);
        if (l2 == 0) return PVector.dist(p, v);
        float t = Math.max(0, Math.min(1, PVector.dot(PVector.sub(p, v), PVector.sub(w, v)) / l2));
        PVector projection = PVector.add(v, PVector.mult(PVector.sub(w, v), t));
        return PVector.dist(p, projection);
    }

    public void display(App app) {
        app.stroke(0);
        app.strokeWeight(10);
        for (int i = 0; i < points.size() - 1; i++) {
            PVector p1 = points.get(i);
            PVector p2 = points.get(i + 1);
            app.line(p1.x, p1.y, p2.x, p2.y);
        }
    }

    public boolean containsPoint(int x, int y) {
        PVector point = new PVector(x, y);
        for (int i = 0; i < points.size() - 1; i++) {
            PVector p1 = points.get(i);
            PVector p2 = points.get(i + 1);
            if (distancePointToSegment(point, p1, p2) <= 5) {
                return true;
            }
        }
        return false;
    }

    public List<PVector> getPoints() {
        return points;
    }
}