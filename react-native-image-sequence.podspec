require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-image-sequence"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.author       = 'git'
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.platform     = :ios, "7.0"
  s.source       = {:git => ''}
  s.source_files  = "ios/RCTImageSequence/*.{h,m}"
  s.dependency "React"
end
