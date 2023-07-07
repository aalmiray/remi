package com.acme;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ASM9;

public class ExtractModuleInfo {
  public static final class ModuleInfo {
    private final String name;
    private final String version;

    public ModuleInfo(String name, Optional<String> version) {
      this.name = Objects.requireNonNull(name);
      this.version = version.orElse(null);
    }

    public String name() {
      return name;
    }

    public Optional<String> version() {
      return Optional.ofNullable(version);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof ModuleInfo)) {
        return false;
      }
      ModuleInfo moduleInfo = (ModuleInfo) obj;
      return name.equals(moduleInfo.name) && Objects.equals(version, moduleInfo.version);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, version);
    }

    @Override
    public String toString() {
      return "ModuleInfo " + name + "@" + version;
    }
  }

  public static ModuleInfo extractModuleInfo(Path moduleInfoPath) throws IOException {
    ClassReader reader;
    try(var inputStream = Files.newInputStream(moduleInfoPath)) {
      reader = new ClassReader(inputStream);
    }
    var classVisitor = new ClassVisitor(ASM9) {
      private ModuleInfo moduleInfo;

      @Override
      public ModuleVisitor visitModule(String name, int access, String version) {
        moduleInfo = new ModuleInfo(name, Optional.ofNullable(version));
        return null;
      }
    };
    reader.accept(classVisitor, 0);
    return classVisitor.moduleInfo;
  }

  public static Set<ModuleInfo> extractRequiredModuleInfo(Path moduleInfoPath) throws IOException {
    ClassReader reader;
    try(var inputStream = Files.newInputStream(moduleInfoPath)) {
      reader = new ClassReader(inputStream);
    }
    var infos = new HashSet<ModuleInfo>();
    var classVisitor = new ClassVisitor(ASM9) {
      @Override
      public ModuleVisitor visitModule(String name, int access, String version) {
        return new ModuleVisitor(ASM9) {
          @Override
          public void visitRequire(String module, int access, String version) {
            infos.add(new ModuleInfo(module, Optional.ofNullable(version)));
          }
        };
      }
    };
    reader.accept(classVisitor, 0);
    return infos;
  }
}
