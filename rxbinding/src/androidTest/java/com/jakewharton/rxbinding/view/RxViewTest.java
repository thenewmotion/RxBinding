package com.jakewharton.rxbinding.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import com.jakewharton.rxbinding.RecordingObserver;
import com.jakewharton.rxbinding.internal.Functions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.Subscription;
import rx.functions.Action1;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.M;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_HOVER_ENTER;
import static android.view.MotionEvent.ACTION_HOVER_EXIT;
import static android.view.MotionEvent.ACTION_HOVER_MOVE;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static com.google.common.truth.Truth.assertThat;
import static com.jakewharton.rxbinding.MotionEventUtil.hoverMotionEventAtPosition;
import static com.jakewharton.rxbinding.MotionEventUtil.motionEventAtPosition;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public final class RxViewTest {
  @Rule public final UiThreadTestRule uiThread = new UiThreadTestRule();

  private final Context context = InstrumentationRegistry.getContext();
  private final View view = new View(context);

  @Test @UiThreadTest public void clicks() {
    RecordingObserver<Void> o = new RecordingObserver<>();
    Subscription subscription = RxView.clicks(view).subscribe(o);
    o.assertNoMoreEvents(); // No initial value.

    view.performClick();
    assertThat(o.takeNext()).isNull();

    view.performClick();
    assertThat(o.takeNext()).isNull();

    subscription.unsubscribe();

    view.performClick();
    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void drags() {
    //RecordingObserver<ViewClickEvent> o = new RecordingObserver<>();
    //Subscription subscription = RxView.clickEvents(view).subscribe(o);
    //o.assertNoMoreEvents(); // No initial value.
    //
    //clock.advance(1, SECONDS);
    //view.performClick();
    //assertThat(o.takeNext()).isEqualTo(ViewClickEvent.create(view, 1000));
    //
    //clock.advance(1, SECONDS);
    //view.performClick();
    //assertThat(o.takeNext()).isEqualTo(ViewClickEvent.create(view, 2000));
    //
    //subscription.unsubscribe();
    //
    //clock.advance(1, SECONDS);
    //view.performClick();
    //o.assertNoMoreEvents();
  }

  @TargetApi(JELLY_BEAN)
  @SdkSuppress(minSdkVersion = JELLY_BEAN)
  @Test @UiThreadTest public void drawEvents() {
    RecordingObserver<Void> o = new RecordingObserver<>();
    Subscription subscription = RxView.draws(view).subscribe(o);
    o.assertNoMoreEvents(); // No initial value.

    view.getViewTreeObserver().dispatchOnDraw();
    assertThat(o.takeNext()).isNull();

    subscription.unsubscribe();

    view.getViewTreeObserver().dispatchOnDraw();
    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void focusChanges() {
    // We need a parent which can take focus from our view when it attempts to clear.
    LinearLayout parent = new LinearLayout(context);
    parent.setFocusable(true);
    parent.addView(view);

    view.setFocusable(true);

    RecordingObserver<Boolean> o = new RecordingObserver<>();
    Subscription subscription = RxView.focusChanges(view).subscribe(o);
    assertThat(o.takeNext()).isFalse();

    view.requestFocus();
    assertThat(o.takeNext()).isTrue();

    view.clearFocus();
    assertThat(o.takeNext()).isFalse();

    subscription.unsubscribe();

    view.requestFocus();
    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void globalLayouts() {
    RecordingObserver<Void> o = new RecordingObserver<>();
    Subscription subscription = RxView.globalLayouts(view).subscribe(o);
    o.assertNoMoreEvents(); // No initial value.

    view.getViewTreeObserver().dispatchOnGlobalLayout();
    assertThat(o.takeNext()).isNull();

    subscription.unsubscribe();
    view.getViewTreeObserver().dispatchOnGlobalLayout();

    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void hovers() {
    RecordingObserver<MotionEvent> o = new RecordingObserver<>();
    Subscription subscription = RxView.hovers(view).subscribe(o);
    o.assertNoMoreEvents();

    view.dispatchGenericMotionEvent(hoverMotionEventAtPosition(view, ACTION_HOVER_ENTER, 0, 50));
    MotionEvent event1 = o.takeNext();
    assertThat(event1.getAction()).isEqualTo(ACTION_HOVER_ENTER);

    view.dispatchGenericMotionEvent(hoverMotionEventAtPosition(view, ACTION_HOVER_MOVE, 1, 50));
    MotionEvent event2 = o.takeNext();
    assertThat(event2.getAction()).isEqualTo(ACTION_HOVER_MOVE);

    subscription.unsubscribe();

    view.dispatchGenericMotionEvent(hoverMotionEventAtPosition(view, ACTION_HOVER_EXIT, 1, 50));
    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void layoutChanges() {
    RecordingObserver<Void> o = new RecordingObserver<>();
    Subscription subscription = RxView.layoutChanges(view).subscribe(o);
    o.assertNoMoreEvents();

    view.layout(view.getLeft() - 5, view.getTop() - 5, view.getRight(), view.getBottom());
    assertThat(o.takeNext()).isNull();

    view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    o.assertNoMoreEvents();

    subscription.unsubscribe();
    view.layout(view.getLeft() - 5, view.getTop() - 5, view.getRight(), view.getBottom());
    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void layoutChangeEvents() {
    RecordingObserver<ViewLayoutChangeEvent> o = new RecordingObserver<>();
    Subscription subscription = RxView.layoutChangeEvents(view).subscribe(o);
    o.assertNoMoreEvents();

    view.layout(view.getLeft() - 5, view.getTop() - 5, view.getRight(), view.getBottom());
    ViewLayoutChangeEvent event1 = o.takeNext();
    assertThat(event1.view()).isSameAs(view);
    assertThat(event1.left()).isNotSameAs(event1.oldLeft());
    assertThat(event1.right()).isSameAs(event1.oldRight());

    view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    o.assertNoMoreEvents();

    subscription.unsubscribe();
    view.layout(view.getLeft() - 5, view.getTop() - 5, view.getRight(), view.getBottom());
    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void longClicks() {
    // We need a parent because long presses delegate to the parent.
    LinearLayout parent = new LinearLayout(context) {
      @Override public boolean showContextMenuForChild(View originalView) {
        return true;
      }
    };
    parent.addView(view);

    RecordingObserver<Void> o = new RecordingObserver<>();
    Subscription subscription = RxView.longClicks(view).subscribe(o);
    o.assertNoMoreEvents(); // No initial value.

    view.performLongClick();
    assertThat(o.takeNext()).isNull();

    view.performLongClick();
    assertThat(o.takeNext()).isNull();

    subscription.unsubscribe();

    view.performLongClick();
    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void preDrawEvents() {
    RecordingObserver<Void> o = new RecordingObserver<>();
    Subscription subscription = RxView.preDraws(view, Functions.FUNC0_ALWAYS_TRUE).subscribe(o);
    o.assertNoMoreEvents(); // No initial value.

    view.getViewTreeObserver().dispatchOnPreDraw();
    assertThat(o.takeNext()).isNull();

    subscription.unsubscribe();

    view.getViewTreeObserver().dispatchOnPreDraw();
    o.assertNoMoreEvents();
  }

  @TargetApi(M)
  @SdkSuppress(minSdkVersion = M)
  @Test @UiThreadTest public void scrollChangeEvents() {
    RecordingObserver<ViewScrollChangeEvent> o = new RecordingObserver<>();
    Subscription subscription = RxView.scrollChangeEvents(view).subscribe(o);
    o.assertNoMoreEvents();

    view.scrollTo(1, 1);
    ViewScrollChangeEvent event0 = o.takeNext();
    assertThat(event0.view()).isSameAs(view);
    assertThat(event0.scrollX()).isEqualTo(1);
    assertThat(event0.scrollY()).isEqualTo(1);
    assertThat(event0.oldScrollX()).isEqualTo(0);
    assertThat(event0.oldScrollY()).isEqualTo(0);

    view.scrollTo(2, 2);
    ViewScrollChangeEvent event1 = o.takeNext();
    assertThat(event1.view()).isSameAs(view);
    assertThat(event1.scrollX()).isEqualTo(2);
    assertThat(event1.scrollY()).isEqualTo(2);
    assertThat(event1.oldScrollX()).isEqualTo(1);
    assertThat(event1.oldScrollY()).isEqualTo(1);

    subscription.unsubscribe();
    view.scrollTo(3, 3);
    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void touches() {
    RecordingObserver<MotionEvent> o = new RecordingObserver<>();
    Subscription subscription = RxView.touches(view).subscribe(o);
    o.assertNoMoreEvents();

    view.dispatchTouchEvent(motionEventAtPosition(view, ACTION_DOWN, 0, 50));
    MotionEvent event1 = o.takeNext();
    assertThat(event1.getAction()).isEqualTo(ACTION_DOWN);

    view.dispatchTouchEvent(motionEventAtPosition(view, ACTION_MOVE, 1, 50));
    MotionEvent event2 = o.takeNext();
    assertThat(event2.getAction()).isEqualTo(ACTION_MOVE);

    subscription.unsubscribe();

    view.dispatchTouchEvent(motionEventAtPosition(view, ACTION_UP, 1, 50));
    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void keys() {
    RecordingObserver<KeyEvent> o = new RecordingObserver<>();
    Subscription subscription = RxView.keys(view).subscribe(o);
    o.assertNoMoreEvents();

    view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_R));
    KeyEvent event1 = o.takeNext();
    assertThat(event1.getAction()).isEqualTo(KeyEvent.ACTION_DOWN);

    view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_H));
    KeyEvent event2 = o.takeNext();
    assertThat(event2.getKeyCode()).isEqualTo(KeyEvent.KEYCODE_H);

    subscription.unsubscribe();

    view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_S));
    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void activated() {
    view.setActivated(true);
    Action1<? super Boolean> action = RxView.activated(view);
    action.call(false);
    assertThat(view.isActivated()).isFalse();
    action.call(true);
    assertThat(view.isActivated()).isTrue();
  }

  @Test @UiThreadTest public void clickable() {
    view.setClickable(true);
    Action1<? super Boolean> action = RxView.clickable(view);
    action.call(false);
    assertThat(view.isClickable()).isFalse();
    action.call(true);
    assertThat(view.isClickable()).isTrue();
  }

  @Test @UiThreadTest public void enabled() {
    view.setEnabled(true);
    Action1<? super Boolean> action = RxView.enabled(view);
    action.call(false);
    assertThat(view.isEnabled()).isFalse();
    action.call(true);
    assertThat(view.isEnabled()).isTrue();
  }

  @Test @UiThreadTest public void pressed() {
    view.setPressed(true);
    Action1<? super Boolean> action = RxView.pressed(view);
    action.call(false);
    assertThat(view.isPressed()).isFalse();
    action.call(true);
    assertThat(view.isPressed()).isTrue();
  }

  @Test @UiThreadTest public void selected() {
    view.setSelected(true);
    Action1<? super Boolean> action = RxView.selected(view);
    action.call(false);
    assertThat(view.isSelected()).isFalse();
    action.call(true);
    assertThat(view.isSelected()).isTrue();
  }

  @Test @UiThreadTest public void visibility() {
    view.setVisibility(View.VISIBLE);
    Action1<? super Boolean> action = RxView.visibility(view);
    action.call(false);
    assertThat(view.getVisibility()).isEqualTo(View.GONE);
    action.call(true);
    assertThat(view.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test @UiThreadTest public void visibilityCustomFalse() {
    view.setVisibility(View.VISIBLE);
    Action1<? super Boolean> action = RxView.visibility(view, View.INVISIBLE);
    action.call(false);
    assertThat(view.getVisibility()).isEqualTo(View.INVISIBLE);
    action.call(true);
    assertThat(view.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @SuppressWarnings("ResourceType") @Test @UiThreadTest public void setVisibilityCustomFalseToVisibleThrows() {
    try {
      RxView.visibility(view, View.VISIBLE);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("Setting visibility to VISIBLE when false would have no effect.");
    }
  }
}
