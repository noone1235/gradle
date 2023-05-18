/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.caching.internal.controller;

import org.gradle.caching.BuildCacheKey;
import org.gradle.caching.internal.NextGenBuildCacheService;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Map;

public interface NextGenBuildCacheAccess extends Closeable {
    <T> void load(Map<BuildCacheKey, T> entries, LoadHandler<T> handler);

    <T> void store(Map<BuildCacheKey, T> entries, StoreHandler<T> handler);

    interface LoadHandler<T> {
        void handle(InputStream input, T payload);
        void startRemoteDownload(BuildCacheKey key);
        void recordRemoteHit(BuildCacheKey key, long size);
        void recordRemoteMiss(BuildCacheKey key);
        void recordRemoteFailure(BuildCacheKey key, Throwable t);
    }

    abstract class DelegatingLoadHandler<T> implements LoadHandler<T> {
        private final LoadHandler<T> delegate;

        public DelegatingLoadHandler(LoadHandler<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void startRemoteDownload(BuildCacheKey key) {
            delegate.startRemoteDownload(key);
        }

        @Override
        public void recordRemoteHit(BuildCacheKey key, long size) {
            delegate.recordRemoteHit(key, size);
        }

        @Override
        public void recordRemoteMiss(BuildCacheKey key) {
            delegate.recordRemoteMiss(key);
        }

        @Override
        public void recordRemoteFailure(BuildCacheKey key, Throwable t) {
            delegate.recordRemoteFailure(key, t);
        }
    }

    interface StoreHandler<T> {
        NextGenBuildCacheService.NextGenWriter handle(T payload);
    }
}
