$HYS_VERSION="@@HYSCALE_BUILD_VERSION@@"
$HYS_JAR_BIN="hyscale-${HYS_VERSION}.jar"
$dir= "$env:LOCALAPPDATA\hyscale"
$fileToCheck = "$dir\$HYS_JAR_BIN"

Function check_java_version
{
  java --version | Out-Null
  If (-NOT  ($? -eq "True")) {
    'Java is not installed'
     exit 1

    }
  $JAVA_VERSION=(Get-Command java | Select-Object -ExpandProperty Version).tostring() | %{ $_.Split('.')[0]; }
  if ($JAVA_VERSION -as [int] -lt 11){
     'JDK version 11 and above is required but found lesser version'
      exit 1
  }
}

Function download_hyscale_jar
{
  if(!(Test-Path -Path $dir )){
      New-Item -ItemType directory -Path $dir | Out-Null
  }

  #Removing old Jar and Downloading the latest jar
  if (!(Test-Path $fileToCheck -PathType leaf))
  {
     rm $dir\hyscale-*.jar
     "Downloading jar file"
     $url="https://github.com/hyscale/hyscale/releases/latest/download/hyscale.jar" 
     try {
       $checkConnection = Invoke-WebRequest -Uri $url -UseBasicParsing
       if ($checkConnection.StatusCode -eq 200) {
         Write-Host "Connection Verified!" -ForegroundColor Green
         wget $url -outfile $HYS_JAR_BIN | Out-Null
       }

     } 
     catch [System.Net.WebException] {
       $exceptionMessage = $Error[0].Exception
       if ($exceptionMessage -match "503") {
         Write-Host "Server Unavaiable" -ForegroundColor Red
         exit 1
       }
       elseif ($exceptionMessage -match "404") {
         Write-Host "Page Not found" -ForegroundColor Red
         exit 1
       }  
     }
     mv $HYS_JAR_BIN $dir
  }
}

check_java_version
download_hyscale_jar

java -Xms216m -Xmx512m -jar $dir\$HYS_JAR_BIN $args

