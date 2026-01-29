@echo off

if "%OS%" == "Windows_NT" setlocal

SET "JAVA_OPTS=-Xms1024m -Xmx1024m -Xmn512m"
SET "ECAMSJV_PORT=29897"

set "CURRENT_DIR=%cd%"
if not "%ECAMSJV_HOME%" == "" goto gotHome
set "ECAMSJV_HOME=%CURRENT_DIR%"
if exist "%ECAMSJV_HOME%\bin\ecamsjv.bat" goto okHome
cd ..
set "ECAMSJV_HOME=%cd%"
cd "%CURRENT_DIR%"
:gotHome
if exist "%ECAMSJV_HOME%\bin\ecamsjv.bat" goto okHome
echo The ECAMSJV_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

set CLASSPATH=



if exist "%ECAMSJV_HOME%\bin\setclasspath.bat" goto okSetclasspath
echo Cannot find "%ECAMSJV_HOME%\bin\setclasspath.bat"
echo This file is needed to run this program
goto end
:okSetclasspath
set "BASEDIR=%ECAMSJV_HOME%"
call "%ECAMSJV_HOME%\bin\setclasspath.bat" %1
if errorlevel 1 goto end

if not "%ECAMSJV_BASE%" == "" goto gotBase
set "ECAMSJV_BASE=%ECAMSJV_HOME%"
:gotBase

if not "%ECAMSJV_TMPDIR%" == "" goto gotTmpdir
set "ECAMSJV_TMPDIR=%ECAMSJV_BASE%\temp"
:gotTmpdir



set "CLASSPATH=%CLASSPATH%%ECAMSJV_HOME%\bin\eCAMS_Server.jar;%ECAMSJV_HOME%\lib;%ECAMSJV_HOME%\conf;%ECAMSJV_HOME%\lib\aopalliance.jar;%ECAMSJV_HOME%\lib\asm-3.2.jar;%ECAMSJV_HOME%\lib\cglib-2.2.jar;%ECAMSJV_HOME%\lib\commons-dbcp-1.2.2.jar;%ECAMSJV_HOME%\lib\commons-lang-2.5.jar;%ECAMSJV_HOME%\lib\commons-logging.jar;%ECAMSJV_HOME%\lib\commons-pool-1.3.jar;%ECAMSJV_HOME%\lib\jackson-core-lgpl-1.4.1.jar;%ECAMSJV_HOME%\lib\jackson-mapper-lgpl-1.4.1.jar;%ECAMSJV_HOME%\lib\log4j-1.2.13.jar;%ECAMSJV_HOME%\lib\log4jdbc3-1.1.jar;%ECAMSJV_HOME%\lib\mybatis-3.0.4-javadoc.jar;%ECAMSJV_HOME%\lib\mybatis-3.0.4-sources.jar;%ECAMSJV_HOME%\lib\mybatis-3.0.4.jar;%ECAMSJV_HOME%\lib\mybatis-spring-1.0.0-javadoc.jar;%ECAMSJV_HOME%\lib\mybatis-spring-1.0.0-sources.jar;%ECAMSJV_HOME%\lib\mybatis-spring-1.0.0.jar;%ECAMSJV_HOME%\lib\netty-3.2.3.Final-sources.jar;%ECAMSJV_HOME%\lib\netty-3.2.3.Final.jar;%ECAMSJV_HOME%\lib\ojdbc5.jar;%ECAMSJV_HOME%\lib\org.springframework.aop-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.asm-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.aspects-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.beans-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.context-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.context.support-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.core-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.expression-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.instrument-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.instrument.tomcat-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.jdbc-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.jms-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.orm-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.oxm-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.test-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.transaction-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.web-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.web.portlet-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.web.servlet-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\org.springframework.web.struts-3.0.5.RELEASE.jar;%ECAMSJV_HOME%\lib\protobuf-java-2.4.0a.jar;%ECAMSJV_HOME%\lib\slf4j-api-1.5.8.jar;%ECAMSJV_HOME%\lib\slf4j-log4j12-1.5.8.jar"


echo Using ECAMSJV_BASE:   "%ECAMSJV_BASE%"
echo Using ECAMSJV_HOME:   "%ECAMSJV_HOME%"
echo Using ECAMSJV_TMPDIR: "%ECAMSJV_TMPDIR%"
if ""%1"" == ""debug"" goto use_jdk
echo Using JRE_HOME:        "%JRE_HOME%"
goto java_dir_displayed
:use_jdk
echo Using JAVA_HOME:       "%JAVA_HOME%"
:java_dir_displayed
echo Using CLASSPATH:       "%CLASSPATH%"

set _EXECJAVA=%_RUNJAVA%
set MAINCLASS=app.Ecams_Server


%_EXECJAVA% %JAVA_OPTS% -Decamsjv.port="%ECAMSJV_PORT%" -classpath "%CLASSPATH%" -Djava.io.tmpdir="%ECAMSJV_TMPDIR%" %MAINCLASS%

:end
