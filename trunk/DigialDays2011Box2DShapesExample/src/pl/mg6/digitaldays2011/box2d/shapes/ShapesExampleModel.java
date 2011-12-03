package pl.mg6.digitaldays2011.box2d.shapes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

import android.graphics.RectF;
import android.util.Log;

public class ShapesExampleModel {

	private static final String TAG = ShapesExampleModel.class.getSimpleName();
	
	private static final Random random = new Random();
	
	private final List<Shape> shapes;
	private final List<RayCastInput> rayCastInputs;
	private RayCastInput newRayCastInput;
	private final List<RayCastOutput> rayCastOutputs;
	private final Transform identityTransform;
	
	private final List<ShapesExampleModelListener> listeners;
	
	public ShapesExampleModel() {
		shapes = new ArrayList<Shape>();
		rayCastInputs = new ArrayList<RayCastInput>();
		rayCastOutputs = new ArrayList<RayCastOutput>();
		identityTransform = new Transform();
		identityTransform.setIdentity();
		listeners = new ArrayList<ShapesExampleModelListener>();
	}
	
	public void createBox(RectF boundingBox) {
		PolygonShape box = new PolygonShape();
		box.setAsBox(
				boundingBox.width() / 2.0f,
				boundingBox.height() / 2.0f,
				new Vec2(boundingBox.centerX(), boundingBox.centerY()),
				random.nextFloat() * MathUtils.PI);
		addShape(box);
	}

	public void createCircle(RectF boundingBox) {
		CircleShape circle = new CircleShape();
		circle.m_p.set(boundingBox.centerX(), boundingBox.centerY());
		circle.m_radius = (boundingBox.width() + boundingBox.height()) / 4.0f;
		addShape(circle);
	}

	public void createTriangle(RectF boundingBox) {
		PolygonShape triangle = new PolygonShape();
		float max = 0.6f * MathUtils.min(boundingBox.width(), boundingBox.height());
		Vec2[] vertices = {
			new Vec2(boundingBox.left + rand(max), boundingBox.top + rand(max)),
			new Vec2(boundingBox.right + rand(max), boundingBox.top + rand(max)),
			new Vec2(boundingBox.centerX() + rand(max), boundingBox.bottom + rand(max)),
		};
		triangle.set(vertices, vertices.length);
		addShape(triangle);
	}

	public void createRaycast(float x, float y) {
		if (rayCastInputs.size() == 5) {
			rayCastInputs.remove(0);
			rayCastOutputs.remove(0);
		}
		newRayCastInput = new RayCastInput();
		newRayCastInput.p1.set(x, y);
		newRayCastInput.maxFraction = 1.0f;
	}
	
	public void updateLastRaycast(float x, float y) {
		if (newRayCastInput != null) {
			rayCastInputs.add(newRayCastInput);
			rayCastOutputs.add(new RayCastOutput());
			newRayCastInput = null;
		}
		RayCastInput input = rayCastInputs.get(rayCastInputs.size() - 1);
		input.p2.set(x, y).subLocal(input.p1).mulLocal(10.0f).addLocal(input.p1);
		updateContacts();
	}

	public void clear() {
		shapes.clear();
		rayCastInputs.clear();
		rayCastOutputs.clear();
		updateContacts();
	}
	
	private static float rand(float max) {
		return (random.nextFloat() - 0.5f) * max;
	}
	
	private void addShape(Shape shape) {
		shapes.add(shape);
		updateContacts();
	}
	
	private void updateContacts() {
		for (int i = 0; i < rayCastInputs.size(); i++) {
			RayCastInput input = rayCastInputs.get(i);
			RayCastOutput output = rayCastOutputs.get(i);
			float smallestFraction = 1.0f;
			Shape smallestFractionShape = null;
			for (Shape shape : shapes) {
				Log.i(TAG, "raycast");
				boolean hit = shape.raycast(output, input, identityTransform);
				if (hit) {
					Log.i(TAG, "hit");
					if (smallestFraction > output.fraction) {
						smallestFraction = output.fraction;
						smallestFractionShape = shape;
					}
				}
			}
			if (smallestFractionShape != null) {
				smallestFractionShape.raycast(output, input, identityTransform);
			} else {
				output.fraction = 1.0f;
			}
		}
		notifyModelChanged();
	}
	
	public List<Shape> getShapes() {
		return shapes;
	}
	
	public List<RayCastInput> getRayCastInputs() {
		return rayCastInputs;
	}
	
	public List<RayCastOutput> getRayCastOutputs() {
		return rayCastOutputs;
	}
	
	public void addListener(ShapesExampleModelListener l) {
		listeners.add(l);
	}
	
	public void removeListener(ShapesExampleModelListener l) {
		listeners.remove(l);
	}
	
	private void notifyModelChanged() {
		for (ShapesExampleModelListener l : listeners) {
			l.modelChanged();
		}
	}
	
	public interface ShapesExampleModelListener {
		void modelChanged();
	}
}
