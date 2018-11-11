# JavaCameraApp
  *To be run with java 8*
### Setup
*Setup steps uses terminology considering Intellij as the editor*
* Install openjdk-8 and openjfx
  <ul>
    <li><code>$ sudo apt install openjdk-8-jdk</code></li>
    <li><code>$ sudo apt install openjdk-8-jre</code></li>
    <li><code>$ sudo apt install openjfx</code></li>
  </ul>
* Add SDK by providing path to <code>java-8-openjdk</code> directory under <code>Project Structure -> SDKs</code>.
* Add <code>opencv-343.jar</code> as a module under <code>File -> Project Structure -> Modules -> Dependencies</code>.
* Add <code>libopencv_java343.so</code> as a native library. To do this, <code>Edit configurations -> VM Options -> Add -Djava.library.path = \<folder containing libopencv_java343.so\></code>.
* Set Language Level 8 in <code>Project Structure -> Project</code> and <code>Project Structure -> Modules -> Dependencies</code> and <code>Project Structure -> Modules -> Sources</code>.
