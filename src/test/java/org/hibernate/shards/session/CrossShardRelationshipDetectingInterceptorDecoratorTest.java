/**
 * Copyright (C) 2007 Google Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.hibernate.shards.session;

import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.shards.defaultmock.InterceptorDefaultMock;
import org.hibernate.type.Type;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;

/**
 * @author maxr@google.com (Max Ross)
 */
public class CrossShardRelationshipDetectingInterceptorDecoratorTest {

    @Test
    public void testOnFlushDirty() {
        final boolean[] onFlushDirtyCalled = {false, false};
        Interceptor interceptor = new InterceptorDefaultMock() {
            @Override
            public boolean onFlushDirty(Object entity, Serializable id,
                                        Object[] currentState, Object[] previousState, String[] propertyNames,
                                        Type[] types) throws CallbackException {
                onFlushDirtyCalled[0] = true;
                return true;
            }
        };

        final ShardIdResolver resolver = new ShardIdResolverDefaultMock();

        CrossShardRelationshipDetectingInterceptor crdi = new CrossShardRelationshipDetectingInterceptor(resolver) {
            @Override
            public boolean onFlushDirty(Object entity, Serializable id,
                                        Object[] currentState, Object[] previousState, String[] propertyNames,
                                        Type[] types) throws CallbackException {
                onFlushDirtyCalled[1] = true;
                return false;
            }
        };

        CrossShardRelationshipDetectingInterceptorDecorator decorator =
                new CrossShardRelationshipDetectingInterceptorDecorator(crdi, interceptor);

        Assert.assertTrue(decorator.onFlushDirty(null, null, null, null, null, null));
        Assert.assertTrue(onFlushDirtyCalled[0]);
        Assert.assertTrue(onFlushDirtyCalled[1]);
    }

    @Test
    public void testOnCollectionUpdate() {
        final boolean[] onCollectionUpdateCalled = {false, false};
        Interceptor interceptor = new InterceptorDefaultMock() {
            @Override
            public void onCollectionUpdate(Object collection, Serializable key)
                    throws CallbackException {
                onCollectionUpdateCalled[0] = true;
            }
        };

        final ShardIdResolver resolver = new ShardIdResolverDefaultMock();

        CrossShardRelationshipDetectingInterceptor crdi = new CrossShardRelationshipDetectingInterceptor(resolver) {
            @Override
            public void onCollectionUpdate(Object collection, Serializable key)
                    throws CallbackException {
                onCollectionUpdateCalled[1] = true;
            }
        };

        CrossShardRelationshipDetectingInterceptorDecorator decorator =
                new CrossShardRelationshipDetectingInterceptorDecorator(crdi, interceptor);

        decorator.onCollectionUpdate(null, null);
        Assert.assertTrue(onCollectionUpdateCalled[0]);
        Assert.assertTrue(onCollectionUpdateCalled[1]);
    }
}
