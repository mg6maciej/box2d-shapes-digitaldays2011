package pl.mg6.digitaldays2011.box2d.shapes;

import java.util.List;

import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Vec2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class ShapesDrawingView extends View {
	
	private static final String TAG = ShapesDrawingView.class.getSimpleName();
	
	private ShapesExampleModel model;
	
	private final Paint paint;
	
	public ShapesDrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(3.0f);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		List<Shape> shapes = model.getShapes();
		for (Shape shape : shapes) {
			if (shape.m_type == ShapeType.CIRCLE) {
				CircleShape circle = (CircleShape) shape;
				paint.setColor(0xCCFF6666);
				canvas.drawCircle(circle.m_p.x, circle.m_p.y, circle.m_radius, paint);
			} else if (shape.m_type == ShapeType.POLYGON) {
				PolygonShape polygon = (PolygonShape) shape;
				Path path = new Path();
				Vec2 vec = polygon.m_vertices[polygon.m_vertexCount - 1];
				path.moveTo(vec.x, vec.y);
				for (int i = 0; i < polygon.m_vertexCount; i++) {
					vec = polygon.m_vertices[i];
					path.lineTo(vec.x, vec.y);
				}
				if (polygon.m_vertexCount == 3) {
					paint.setColor(0xCC66FF66);
				} else {
					paint.setColor(0xCC6666FF);
				}
				canvas.drawPath(path, paint);
			}
		}
		List<RayCastInput> inputs = model.getRayCastInputs();
		List<RayCastOutput> outputs = model.getRayCastOutputs();
		for (int i = 0; i < inputs.size(); i++) {
			RayCastInput input = inputs.get(i);
			RayCastOutput output = outputs.get(i);
			Vec2 contact = input.p2.sub(input.p1).mulLocal(output.fraction).addLocal(input.p1);
			paint.setColor(0xFFFFFFFF);
			canvas.drawLine(input.p1.x, input.p1.y, contact.x, contact.y, paint);
			if (output.fraction != 1.0f) {
				Vec2 reflected = input.p2.sub(contact);
				reflected = output.normal.mul(Vec2.dot(reflected, output.normal));
				reflected = input.p2.sub(reflected.mulLocal(2.0f));
				paint.setColor(0x88FFFF88);
				canvas.drawLine(contact.x, contact.y, reflected.x, reflected.y, paint);
				paint.setColor(0xFF66FF66);
				canvas.drawLine(contact.x, contact.y, contact.x + 50.0f * output.normal.x, contact.y + 50.0f * output.normal.y, paint);
				paint.setColor(0xFFFFFFFF);
				canvas.drawCircle(contact.x, contact.y, 3.0f, paint);
			}
		}
	}

	public void setModel(ShapesExampleModel newModel) {
		model = newModel;
	}
}
