package pl.mg6.digitaldays2011.box2d.shapes;

import java.util.ArrayList;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class ShapesExampleActivity extends Activity implements ShapesExampleModel.ShapesExampleModelListener {
	
	private static final String TAG = ShapesExampleActivity.class.getSimpleName();
	
	private static final String GESTURE_ID_TRIANGLE = "triangle";
	private static final String GESTURE_ID_CIRCLE = "circle";
	private static final String GESTURE_ID_BOX = "box";
	
	private ShapesExampleModel model;
	
	private GestureOverlayView gestureOverlayView;
	private ShapesDrawingView shapesDrawingView;
	private Button toggleDrawRaycast;
	
	private static final String KEY_RAYCASTING = "pl.mg6.d.b.s.ShapesExampleActivity.raycasting";
	
	private boolean raycasting;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shapes_example_view);
		
		model = (ShapesExampleModel) getLastNonConfigurationInstance();
		if (model == null) {
			model = new ShapesExampleModel();
		}
		model.addListener(this);
		
		final GestureLibrary gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		gestureLibrary.load();
		
		gestureOverlayView = (GestureOverlayView) findViewById(R.id.gesture_overlay_view);
		gestureOverlayView.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
			public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
				ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);
				if (predictions.size() > 0) {
					Prediction prediction = predictions.get(0);
					Log.i(TAG, "prediction: " + prediction.name + "; score: " + prediction.score);
					if (prediction.score >= 2.0) {
						String gestureId = prediction.name;
						RectF boundingBox = gesture.getBoundingBox();
						if (GESTURE_ID_BOX.equals(gestureId)) {
							model.createBox(boundingBox);
						} else if (GESTURE_ID_CIRCLE.equals(gestureId)) {
							model.createCircle(boundingBox);
						} else if (GESTURE_ID_TRIANGLE.equals(gestureId)) {
							model.createTriangle(boundingBox);
						}
					}
				} else {
					Log.i(TAG, "gesture not recognized");
				}
			}
		});
		shapesDrawingView = (ShapesDrawingView) findViewById(R.id.shapes_drawing_view);
		shapesDrawingView.setModel(model);
		shapesDrawingView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (raycasting) {
					int action = event.getAction() & MotionEvent.ACTION_MASK;
					if (action == MotionEvent.ACTION_DOWN) {
						model.createRaycast(event.getX(), event.getY());
					} else if (action == MotionEvent.ACTION_MOVE) {
						model.updateLastRaycast(event.getX(), event.getY());
					}
					return true;
				}
				return false;
			}
		});
		toggleDrawRaycast = (Button) findViewById(R.id.toggle_draw_raycast);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		model.removeListener(this);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return model;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_RAYCASTING, raycasting);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		setRaycasting(savedInstanceState.getBoolean(KEY_RAYCASTING));
	}
	
	public void modelChanged() {
		shapesDrawingView.invalidate();
	}
	
	public void onToggleDrawRaycastClick(View view) {
		setRaycasting(!raycasting);
	}

	public void setRaycasting(boolean value) {
		raycasting = value;
		toggleDrawRaycast.setText(raycasting ? R.string.draw : R.string.raycast);
	}
	
	public void onClearClick(View view) {
		model.clear();
	}
}
