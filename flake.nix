
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
        pgData = "/tmp/erp-pgdata";
      in
      {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            jdk
            maven
            git
            docker
            docker-compose
            postgresql
            heroku
          ];
          shellHook = ''
            export DOCKER_HOST="unix:///run/user/$UID/podman/podman.sock"
            if ! systemctl --user is-active --quiet podman.socket 2>/dev/null; then
              systemctl --user start podman.socket 2>/dev/null || true
            fi

            # PostgreSQL setup
            export PGDATA="${pgData}"
            export PGHOST=localhost
            export PGPORT=5432
            export PGUSER=erp
            export PGDATABASE=erp

            if [ ! -d "$PGDATA" ]; then
              echo "Initializing PostgreSQL in $PGDATA..."
              initdb -D "$PGDATA" --auth=trust
            fi

            echo "Starting PostgreSQL..."
            pg_ctl -D "$PGDATA" -l "$PGDATA/pg.log" start 2>/dev/null || true
            sleep 2

            # Create user and database if they don't exist
            psql -d postgres -c "SELECT 1 FROM pg_roles WHERE rolname='erp'" | grep -q 1 || \
              createuser -s erp 2>/dev/null || true
            psql -d postgres -lqt | cut -d \| -f 1 | grep -qw erp || \
              createdb -O erp erp 2>/dev/null || true

            echo ""
            echo "PostgreSQL is running on localhost:5432"
            echo "Database: erp | User: erp"
            echo ""
            echo "Start the app with:"
            echo "  mvn spring-boot:run -Dspring-boot.run.profiles=dev"
            echo ""
            echo "Heroku commands:"
            echo "  heroku login"
            echo "  heroku create your-app-name"
            echo "  heroku addons:create heroku-postgresql:essential-0"
            echo ""

            # Cleanup on exit
            trap "echo 'Stopping PostgreSQL...'; pg_ctl -D $PGDATA stop 2>/dev/null || true" EXIT
          '';
        };
      }
    );
}
