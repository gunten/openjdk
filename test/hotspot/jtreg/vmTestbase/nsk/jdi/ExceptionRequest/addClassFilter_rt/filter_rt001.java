/*
 * Copyright (c) 2001, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package nsk.jdi.ExceptionRequest.addClassFilter_rt;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdi.*;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

import java.util.*;
import java.io.*;

/**
 * The test for the implementation of an object of the type
 * ExceptionRequest.
 *
 * The test checks that results of the method
 * <code>com.sun.jdi.ExceptionRequest.addClassFilter(ReferenceType)</code>
 * complies with its spec.
 *
 * The test checks up on the following assertion:
 *    Restricts the events generated by this request to those
 *    whose location is in the given reference type or
 *    any of its subtypes.
 *
 * The test works as follows.
 * - The debugger resumes the debuggee and waits for the BreakpointEvent.
 * - The debuggee creates two threads, thread1 and thread2
 *   (thread1 will invoke methods throwing NullPointerException in
 *   the super-class TestClass10 and its sub-class TestClass11),
 *   and invokes the methodForCommunication to be suspended and
 *   to inform the debugger with the event.
 * - Upon getting the BreakpointEvent, the debugger
 *   - gets ReferenceTypes to use to filter ExceptionEvents,
 *   - sets up ExceptionRequest for the events,
 *   - restricts the events to those in thread1,
 *   - and resumes the debuggee and waits for the events.
 * - The debuggee starts threads.
 * - Upon getting the events, the debugger performs checks required.
 */

public class filter_rt001 extends TestDebuggerType1 {

    public static void main (String argv[]) {
        System.exit(run(argv, System.out) + Consts.JCK_STATUS_BASE);
    }

    public static int run (String argv[], PrintStream out) {
        debuggeeName = "nsk.jdi.ExceptionRequest.addClassFilter_rt.filter_rt001a";
        return new filter_rt001().runThis(argv, out);
    }

    private String testedClassName =
      "nsk.jdi.ExceptionRequest.addClassFilter_rt.filter_rt001aTestClass11";

    protected void testRun() {
        EventRequest  eventRequest1      = null;
        String        property1          = "ExceptionRequest1";

        for (int i = 0; ; i++) {

            if (!shouldRunAfterBreakpoint()) {
                vm.resume();
                break;
            }

            display(":::::: case: # " + i);

            switch (i) {

                case 0:
                final ReferenceType testClassReference = (ReferenceType)debuggee.classByName(testedClassName);

                eventRequest1 = setting21ExceptionRequest( null, testClassReference,
                                             EventRequest.SUSPEND_ALL, property1);

                display("......waiting for ExceptionEvent in expected class");
                Event newEvent = eventHandler.waitForRequestedEvent(new EventRequest[]{eventRequest1}, waitTime, true);

                if ( !(newEvent instanceof ExceptionEvent)) {
                    setFailedStatus("New event is not ExceptionEvent");
                } else {
                    String property = (String) newEvent.request().getProperty("number");
                    if ( !property.equals(property1) )
                        setFailedStatus("Received ExceptionEvent with unexpected property: " + property1);

                    ReferenceType eventDeclType = ((ExceptionEvent)newEvent).location().declaringType();
                    if (!eventDeclType.equals(testClassReference)) {
                        setFailedStatus("Received unexpected ExceptionEvent for class:" + eventDeclType.name());
                    } else {
                        display("Received expected ExceptionEvent for " + eventDeclType.name());
                    }
                }

                vm.resume();
                break;

                default:
                throw new Failure("** default case 1 **");
            }
        }
        return;
    }

    private ExceptionRequest setting21ExceptionRequest ( ThreadReference thread,
                                                         ReferenceType   testedClass,
                                                         int             suspendPolicy,
                                                         String          property       ) {
        try {
            display("......setting up ExceptionRequest:");
            display("       thread: " + thread + "; class: " + testedClass + "; property: " + property);

            ExceptionRequest
            excr = eventRManager.createExceptionRequest(null, true, true);
            excr.putProperty("number", property);
            if (thread != null)
                excr.addThreadFilter(thread);
            excr.addClassFilter(testedClass);
            excr.setSuspendPolicy(suspendPolicy);

            display("      ExceptionRequest has been set up");
            return excr;
        } catch ( Exception e ) {
            throw new Failure("** FAILURE to set up ExceptionRequest **");
        }
    }
}
