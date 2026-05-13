{
  description = "ERP Backend Dev Shell";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            jdk
            maven
            git
            docker
            docker-compose
          ];
          shellHook = ''
            export DOCKER_HOST="unix:///run/user/$UID/podman/podman.sock"
            if ! systemctl --user is-active --quiet podman.socket 2>/dev/null; then
              systemctl --user start podman.socket 2>/dev/null || true
            fi
          '';
        };
      }
    );
}
