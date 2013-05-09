#!C:\strawberry\perl\bin\perl.exe

use strict;
use CGI qw/param header/;
print header('text/plain');

my $JAVACLASS = "com.company.Main";

# Read data
my $holding = param("holding");
my $world = param("world");
my $trees = param("trees");
# Remove whitespace
$holding =~ s/\s//g;
$world =~ s/\s//g;
#$trees =~ s/\s//g;

# Call Java
exec "java -classpath \".\\out\\production\\planner\" $JAVACLASS \"$holding\" \"$world\" \"$trees\"";
