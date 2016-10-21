package com.jakewharton.rxbinding.support.v4.widget;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.widget.SlidingPaneLayout;
import rx.Observable;
import rx.functions.Action1;

import static com.jakewharton.rxbinding.internal.Preconditions.checkNotNull;

/**
 * Static factory methods for creating {@linkplain Observable observables}
 * for {@link SlidingPaneLayout}
 */
public final class RxSlidingPaneLayout {
  /**
   * Create an observable of the open state of the pane of {@code view}
   * <p>
   * <em>Warning:</em> The created observable keeps a strong reference to {@code view}. Unsubscribe
   * to free this reference.
   * <p>
   * <em>Warning:</em> The created observable uses {@link SlidingPaneLayout#setPanelSlideListener}
   * to observe dismiss change. Only one observable can be used for a view at a time.
   * <p>
   * <em>Note:</em> A value will be emitted immediately on subscribe.
   */
  @CheckResult @NonNull public static Observable<Boolean> panelOpens(
      @NonNull SlidingPaneLayout view) {
    checkNotNull(view, "view == null");
    return Observable.create(new SlidingPaneLayoutPaneOpenedOnSubscribe(view));
  }

  /**
   * Create an observable of the slide offset of the pane of {@code view}
   * <p>
   * <em>Warning:</em> The created observable keeps a strong reference to {@code view}. Unsubscribe
   * to free this reference.
   * <p>
   * <em>Warning:</em> The created observable uses {@link SlidingPaneLayout#setPanelSlideListener}
   * to observe dismiss change. Only one observable can be used for a view at a time.
   */
  @CheckResult @NonNull public static Observable<Float> panelSlides(
      @NonNull SlidingPaneLayout view) {
    checkNotNull(view, "view == null");
    return Observable.create(new SlidingPaneLayoutSlideOnSubscribe(view));
  }

  /**
   * An action which sets whether the pane of {@code view} is open.
   * <p>
   * <em>Warning:</em> The created observable keeps a strong reference to {@code view}. Unsubscribe
   * to free this reference.
   */
  @CheckResult @NonNull public static Action1<? super Boolean> open(
      @NonNull final SlidingPaneLayout view) {
    checkNotNull(view, "view == null");
    return new Action1<Boolean>() {
      @Override public void call(Boolean value) {
        if (value) {
          view.openPane();
        } else {
          view.closePane();
        }
      }
    };
  }

  private RxSlidingPaneLayout() {
    throw new AssertionError("No instances.");
  }
}
