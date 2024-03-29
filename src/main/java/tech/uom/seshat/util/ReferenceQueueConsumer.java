/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership. You may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
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
import tech.uom.seshat.resources.Errors;


/**
 * A thread processing all {@link Reference} instances enqueued in a {@link ReferenceQueue}.
 * This is the central place where <em>every</em> weak references produced by the Seshat library
 * are consumed. This thread will invoke the {@link WeakEntry#dispose()} method for each
 * references enqueued by the garbage collector.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.2
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
     * <div class="note"><b>Note:</b>
     * We give to this thread a priority higher than the normal one since this thread shall
     * execute only tasks to be completed very shortly. Quick execution of those tasks is at
     * the benefit of the rest of the system, since they make more resources available sooner.</div>
     */
    private ReferenceQueueConsumer() {
        super(null, null, "Seshat disposer", 16*1024);        // Small (16 kb) stack size is sufficient.
        setPriority(Thread.MAX_PRIORITY - 2);
        setDaemon(true);
    }

    /**
     * Loop to be run during the virtual machine lifetime.
     * Public as an implementation side-effect; <strong>do not invoke explicitly!</strong>
     */
    @Override
    public final void run() {
        /*
         * The reference queue should never be null. However, some strange cases have been
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
                     * If the reference does not extend the WeakEntry<?> class, we want the
                     * ClassCastException to be logged in the "catch" block since it would be
                     * a programming error that we want to know about.
                     */
                    ((WeakEntry<?>) ref).dispose();
                }
            } catch (Throwable exception) {
                Errors.getLogger().log(System.Logger.Level.WARNING, exception);
            }
        }
        // Do not log anything at this point, since the loggers may be shutdown now.
    }
}
