import sys

JAVA_PROGRAM_TEMPLATE = 'public class Program { public static String CODE = %s; }\n'

def convert(s):
  # Ugh stupid java.
  return '+'.join('Character.toString((char) %d)' % ord(c) for c in s)

if __name__ == '__main__':
  sys.stdout.write(JAVA_PROGRAM_TEMPLATE % convert(sys.stdin.read()))
