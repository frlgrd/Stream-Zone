package com.frlgrd.streamzone.core.event;

import com.squareup.otto.Bus;

import org.androidannotations.annotations.EBean;

@EBean(scope = EBean.Scope.Singleton)
public class Otto extends Bus {
}
