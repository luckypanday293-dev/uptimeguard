Write-Host "Starting PostgreSQL..."
docker compose up -d
Write-Host "PostgreSQL started."
Write-Host "Start backend in another terminal: cd backend; mvn spring-boot:run"
Write-Host "Start frontend in another terminal: cd frontend; npm.cmd install; npm.cmd run dev"
