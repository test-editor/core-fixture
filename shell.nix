with import <nixpkgs> {};

let

testeditor = pkgs.callPackage (import (builtins.fetchGit {
      url = "https://github.com/test-editor/nix-packages";
    })) {};

in

stdenv.mkDerivation {
    name = "test-editor-xtext-gradle";
    buildInputs = [
        testeditor.openjdk_10_0_2
        travis
    ];
    shellHook = ''
        # do some gradle "finetuning"
        alias g="./gradlew"
        alias g.="../gradlew"
        export GRADLE_OPTS="$GRADLE_OPTS -Dorg.gradle.daemon=false -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
        export JAVA_TOOL_OPTIONS="$_JAVA_OPTIONS  -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
    '';
}
