# Etapa de construcción
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar archivos necesarios
COPY pom.xml .
# IMPORTANTE: Asegúrate de que mvnw y la carpeta .mvn NO estén en tu .dockerignore
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear usuario de seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# --- AJUSTES PARA RENDER FREE ---
ENV SPRING_PROFILES_ACTIVE=production
# He bajado la memoria a 380m para que no se sature el contenedor de 512m
ENV JAVA_OPTS="-Xmx380m -Xms128m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"
# Ajustado a tu zona horaria
ENV TZ=America/Lima

# SOLO USA ESTO SI TIENES LA DEPENDENCIA 'ACTUATOR' EN TU POM.XML
# Si no la tienes, borra o comenta estas dos lineas:
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]