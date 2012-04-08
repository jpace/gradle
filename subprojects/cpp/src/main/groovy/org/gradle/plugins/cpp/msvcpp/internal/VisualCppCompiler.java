/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.plugins.cpp.msvcpp.internal;

import org.gradle.internal.Factory;
import org.gradle.plugins.cpp.compiler.internal.ArgWriter;
import org.gradle.plugins.cpp.compiler.internal.CommandLineCppCompiler;
import org.gradle.plugins.cpp.compiler.internal.CommandLineCppCompilerArgumentsToOptionFile;
import org.gradle.plugins.cpp.gpp.GppCompileSpec;
import org.gradle.process.internal.ExecAction;

import java.io.File;

class VisualCppCompiler extends CommandLineCppCompiler<GppCompileSpec> {

    VisualCppCompiler(File executable, Factory<ExecAction> execActionFactory) {
        super(executable, execActionFactory, new CommandLineCppCompilerArgumentsToOptionFile<GppCompileSpec>(
                ArgWriter.windowsStyleFactory(), new VisualCppCompileSpecToArguments()
        ));
    }

}
