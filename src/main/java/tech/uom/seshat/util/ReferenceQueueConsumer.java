/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.uom.seshat.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.logging.Logger;


/**
 * A thread processing all {@link Reference} instances enqueued in a {@link ReferenceQueue}.
 * This is the central place where <em>every</em> weak references produced by the SIS library
 * are consumed. This thread will invoke the {@link WeakEntry#dispose()} method for each
 * references enqueued by the garbage collector.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class ReferenceQueueConsumer extends Thread {
    /**
     * List of references collected by the garbage collector.
     * This reference shall be given to {@link Reference} constructors.
     */
    static final ReferenceQueue<Object> QUEUE = new ReferenceQueue<>();

    /**
     * Creates the singleton instance of the {@code ReferenceQueueConsumer} thread.
     */
    static {
        final ReferenceQueueConsumer thread = new ReferenceQueueConsumer();
        /*
         * Call to Thread.start() must be outside the constructor
         * (Reference: Goetz et al.: "Java Concurrency in Practice").
         */
        thread.start();
    }

    /**
     * Constructs a new thread as a daemon thread. This thread will be sleeping most of the time.
     * It will run only only a few nanoseconds every time a new {@link Reference} is enqueued.
     *
     * <p>We give to this thread a priority higher than the normal one since this thread shall
     * execute only tasks to be completed very shortly. Quick execution of those tasks is at
     * the benefit of the rest of the system, since they make more resources available sooner.</p>
     */
    private ReferenceQueueConsumer() {
        super("SeshatDisposer");
        setPriority(Thread.MAX_PRIORITY - 2);
    }

    /**
     * Loop to be run during the virtual machine lifetime.
     * Public as an implementation side-effect; <strong>do not invoke explicitly!</strong>
     */
    @Override
    public final void run() {
        /*
         * The reference queue should never be null. However some strange cases have been
         * observed at shutdown time. If the field become null, assume that a shutdown is
         * under way and let the thread terminate.
         */
        ReferenceQueue<Object> queue;
        while ((queue = QUEUE) != null) {
            try {
                /*
                 * Block until a reference is enqueued. The reference should never be null
                 * when using the method without timeout (it could be null if we specified
                 * a timeout). If the remove() method behaves as if a timeout occurred, we
                 * may be in the middle of a shutdown. Continue anyway as long as we didn't
                 * received the kill event.
                 */
                final Reference<?> ref = queue.remove();
                if (ref != null) {
                    /*
                     * If the reference does not implement the WeakEntry<?> class, we want the
                     * ClassCastException to be logged in the "catch" block since it would be
                     * a programming error that we want to know about.
                     */
                    ((WeakEntry<?>) ref).dispose();
                }
            } catch (Throwable exception) {
                Logger.getLogger("tech.uom.seshat").warning(exception.toString());
            }
        }
        // Do not log anything at this point, since the loggers may be shutdown now.
    }
}
