package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class ConvexHullGrahamScan extends Application {
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;

	private List<Point> points = new ArrayList<>();
	private Stack<Point> convexHull = new Stack<>();
	private Canvas canvas;
	private GraphicsContext gc;
	private boolean isAnimationStarted = false;
	private Timeline animationTimeline;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Convex Hull Graham Scan");
		canvas = new Canvas(WIDTH, HEIGHT);
		gc = canvas.getGraphicsContext2D();

		Pane root = new Pane(canvas);
		Scene scene = new Scene(root);

		scene.setOnMouseClicked(this::handleMouseClick);
		scene.setOnKeyPressed(this::handleKeyPress);

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void handleMouseClick(MouseEvent event) {
		if (!isAnimationStarted) {
			double x = event.getX();
			double y = event.getY();
			Point newPoint = new Point(x, y);
			points.add(newPoint);
			drawPoint(newPoint);
		}
	}

	private void handleKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.SPACE && !isAnimationStarted && points.size() >= 3) {
			startAnimation();
		}
	}

	private void drawPoint(Point point) {
		
		gc.fillOval(point.getX() - 3, point.getY() - 3, 6, 6);
	}

	private void calculateConvexHull() {
		if (points.size() < 3) {
			Label l = new Label("Points are les than 3");
			return;
		}

		gc.clearRect(0, 0, WIDTH, HEIGHT);

		Point minY = points.get(0);
		int minIndex = 0;

		// Find the point with the lowest Y coordinate (and leftmost if tied)
		for (int i = 1; i < points.size(); i++) {
			Point current = points.get(i);
			if (current.getY() < minY.getY() || (current.getY() == minY.getY() && current.getX() < minY.getX())) {
				minY = current;
				minIndex = i;
			}
		}

		// Move the point with the lowest Y coordinate to the front
		Collections.swap(points, 0, minIndex);

		// Sort the points based on polar angle with respect to minY
		Collections.sort(points.subList(1, points.size()), minY.polarOrder());

		convexHull.push(points.get(0));
		convexHull.push(points.get(1));

		for (int i = 2; i < points.size(); i++) {
			while (convexHull.size() > 1
					&& !isCCW(convexHull.get(convexHull.size() - 2), convexHull.peek(), points.get(i))) {
				convexHull.pop();
			}
			convexHull.push(points.get(i));
		}

		drawConvexHull(convexHull);
	}

	private void drawConvexHull(Stack<Point> hull) {
		
		gc.setStroke(Color.BLACK);
		gc.beginPath();
		if (hull.size() < 2) {
			return;
		}

		Point first = hull.get(0);
		gc.moveTo(first.getX(), first.getY());

		for (Point point : hull) {
			gc.lineTo(point.getX(), point.getY());
		}
		gc.lineTo(first.getX(), first.getY());
		gc.stroke();

		gc.setFill(Color.BLACK);
		for (Point p : points) {
			drawPoint(p);
		
		}
	}

	private boolean isCCW(Point a, Point b, Point c) {
		double crossProduct = (b.getX() - a.getX()) * (c.getY() - a.getY())
				- (b.getY() - a.getY()) * (c.getX() - a.getX());
		return crossProduct > 0;
	}

	private void startAnimation() {
		isAnimationStarted = true;
		animationTimeline = new Timeline(new KeyFrame(Duration.millis(500), event -> {
			calculateConvexHull();
			isAnimationStarted = false;
		}));
		animationTimeline.setCycleCount(1);
		animationTimeline.play();
	}

	public static class Point implements Comparable<Point> {
		private double x;
		private double y;

		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		@Override
		public int compareTo(Point other) {
			return Double.compare(this.x, other.x);
		}

		public PolarOrder polarOrder() {
			return new PolarOrder();
		}

		public class PolarOrder implements Comparator<Point> {
			@Override
			public int compare(Point q1, Point q2) {
				double angle1 = Math.atan2(q1.getY() - y, q1.getX() - x);
				double angle2 = Math.atan2(q2.getY() - y, q2.getX() - x);

				if (angle1 < angle2) {
					return -1;
				} else if (angle1 > angle2) {
					return 1;
				} else {
					double distance1 = (x - q1.getX()) * (x - q1.getX()) + (y - q1.getY()) * (y - q1.getY());
					double distance2 = (x - q2.getX()) * (x - q2.getX()) + (y - q2.getY()) * (y - q2.getY());

					return Double.compare(distance1, distance2);
				}
			}
		}
	}
}
