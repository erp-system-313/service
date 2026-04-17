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
            # Docker daemon check
            if docker info > /dev/null 2>&1; then
              echo "Docker daemon is running ✓"
              
              # Start PostgreSQL via docker-compose if available
              if [ -f docker-compose.yml ]; then
                echo "Starting services with docker-compose..."
                docker-compose up -d
              fi
            else
              echo "⚠ Docker daemon is not running"
              echo "To enable Docker:"
              echo "  - On NixOS: add 'services.docker.enable = true;' to your configuration.nix"
              echo "  - On Linux: sudo systemctl start docker"
              echo "  - On macOS: start Docker Desktop"
              echo ""
              echo "You can still develop without Docker - Spring Boot will connect to external services."
            fi

            echo "Development environment ready!"
          '';
        };
      }
    );
}
