package com.jakewharton.rxbinding.support.v4.view;

import android.support.test.annotation.UiThreadTest;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.ViewPager;
import com.jakewharton.rxbinding.RecordingObserver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
public final class RxViewPagerTest {
  @Rule public final ActivityTestRule<RxViewPagerTestActivity> activityRule =
      new ActivityTestRule<>(RxViewPagerTestActivity.class);

  private ViewPager view;

  @Before public void setUp() {
    RxViewPagerTestActivity activity = activityRule.getActivity();
    view = activity.viewPager;
  }

  @Test public void pageScrollStateChanges() {
    view.setCurrentItem(0);
    RecordingObserver<Integer> o = new RecordingObserver<>();
    Subscription subscription = RxViewPager.pageScrollStateChanges(view)
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(o);
    o.assertNoMoreEvents(); // No initial value.

    onView(withId(1)).perform(swipeLeft());
    assertThat(o.takeNext()).isEqualTo(ViewPager.SCROLL_STATE_DRAGGING);
    assertThat(o.takeNext()).isEqualTo(ViewPager.SCROLL_STATE_SETTLING);
    assertThat(o.takeNext()).isEqualTo(ViewPager.SCROLL_STATE_IDLE);
    o.assertNoMoreEvents();

    subscription.unsubscribe();

    onView(withId(1)).perform(swipeLeft());
    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void pageSelections() {
    view.setCurrentItem(0);
    RecordingObserver<Integer> o = new RecordingObserver<>();
    Subscription subscription = RxViewPager.pageSelections(view).subscribe(o);
    assertThat(o.takeNext()).isEqualTo(0);

    view.setCurrentItem(3);
    assertThat(o.takeNext()).isEqualTo(3);
    view.setCurrentItem(5);
    assertThat(o.takeNext()).isEqualTo(5);

    subscription.unsubscribe();

    view.setCurrentItem(0);
    o.assertNoMoreEvents();
  }

  @Test @UiThreadTest public void currentItem() {
    Action1<? super Integer> action = RxViewPager.currentItem(view);
    action.call(3);
    assertThat(view.getCurrentItem()).isEqualTo(3);
    action.call(5);
    assertThat(view.getCurrentItem()).isEqualTo(5);
  }

  private static ViewAction swipeLeft() {
    return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER_RIGHT,
        GeneralLocation.CENTER_LEFT, Press.FINGER);
  }
}
