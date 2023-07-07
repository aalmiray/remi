package com.acme;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ASM9;

public class ModuleInfoRewriter {
  public void rewrite(Path moduleInfoPath, Map<String, String> moduleVersionMap) throws IOException {
    ClassReader reader;
    try(var inputStream = Files.newInputStream(moduleInfoPath)) {
      reader = new ClassReader(inputStream);
    }
    var writer = new ClassWriter(reader, 0);
    reader.accept(new ClassVisitor(ASM9, writer) {
      @Override
      public ModuleVisitor visitModule(String name, int access, String version) {
        var moduleVisitor = super.visitModule(name, access, version);
        return new ModuleVisitor(ASM9, moduleVisitor) {
          @Override
          public void visitRequire(String moduleName, int access, String oldVersion) {
            var newVersion = moduleVersionMap.getOrDefault(moduleName, oldVersion);
            super.visitRequire(moduleName, access, newVersion);
          }
        };
      }
    }, 0);
    Files.write(moduleInfoPath, writer.toByteArray());
  }
}
