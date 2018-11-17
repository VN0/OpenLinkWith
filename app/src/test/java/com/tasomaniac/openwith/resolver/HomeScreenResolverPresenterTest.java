package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.content.res.Resources;
import com.tasomaniac.openwith.R;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

public class HomeScreenResolverPresenterTest {

    private static final String TITLE = "title";
    private static final IntentResolverResult EMPTY_RESULT = new IntentResolverResult(Collections.emptyList(), null, false);

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    @Mock private IntentResolver intentResolver;
    @Mock private ResolverView view;
    @Mock private Resources resources;
    @Mock private Intent sourceIntent;
    @Mock private ResolverNavigation navigation;

    private HomeScreenResolverPresenter presenter;

    @Before
    public void setUp() {
        presenter = new HomeScreenResolverPresenter(resources, intentResolver);
        presenter.bind(view, navigation);
    }

    @Test
    public void unbindShouldNullifyListeners() {
        presenter.unbind(view);

        then(view).should().setListener(null);
        then(intentResolver).should().unbind();
    }

    @Test
    public void shouldHaveNoInteractionWithFilteredItem() {
        IntentResolver.Listener listener = captureIntentResolverListener();

        DisplayActivityInfo filteredItem = mock(DisplayActivityInfo.class);
        listener.onIntentResolved(new IntentResolverResult(Collections.emptyList(), filteredItem, false));

        then(filteredItem).shouldHaveZeroInteractions();
    }

    @Test
    public void givenEmptyResolveListShouldDisplayWarningAndDismiss() {
        given(intentResolver.getSourceIntent()).willReturn(sourceIntent);
        IntentResolver.Listener listener = captureIntentResolverListener();
        reset(view);

        listener.onIntentResolved(EMPTY_RESULT);

        then(view).should().toast(R.string.empty_resolver_activity);
        then(navigation).should().dismiss();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void givenResolveListShouldSetupUI() {
        IntentResolver.Listener listener = captureIntentResolverListener();

        DisplayActivityInfo item = mock(DisplayActivityInfo.class);
        IntentResolverResult result = resultWithItem(item);
        listener.onIntentResolved(result);

        then(view).should().displayData(result);
    }

    @Test
    public void givenResolveListShouldDisplayTitle() {
        IntentResolver.Listener listener = captureIntentResolverListener();
        given(resources.getString(R.string.add_to_homescreen)).willReturn(TITLE);

        DisplayActivityInfo item = mock(DisplayActivityInfo.class);
        listener.onIntentResolved(resultWithItem(item));

        then(view).should().setTitle(TITLE);
    }

    @Test
    public void shouldReloadWhenPackagedChanged() {
        ResolverView.Listener listener = captureViewListener();
        reset(intentResolver);

        listener.onPackagesChanged();

        then(intentResolver).should().resolve();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfActionButtonsClicked() {
        ResolverView.Listener listener = captureViewListener();

        listener.onActionButtonClick(false);
    }

    private IntentResolver.Listener captureIntentResolverListener() {
        ArgumentCaptor<IntentResolver.Listener> argumentCaptor = ArgumentCaptor.forClass(IntentResolver.Listener.class);
        then(intentResolver).should(atLeastOnce()).bind(argumentCaptor.capture());
        return argumentCaptor.getValue();
    }

    private ResolverView.Listener captureViewListener() {
        ArgumentCaptor<ResolverView.Listener> argumentCaptor = ArgumentCaptor.forClass(ResolverView.Listener.class);
        then(view).should().setListener(argumentCaptor.capture());
        return argumentCaptor.getValue();
    }

    private static IntentResolverResult resultWithItem(DisplayActivityInfo item) {
        return new IntentResolverResult(Collections.singletonList(item), null, false);
    }
}
