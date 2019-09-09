require "json"

 package = JSON.parse(File.read(File.join(__dir__, "package.json")))

 Pod::Spec.new do |s|
  s.name         = package["name"]
  s.version      = package["version"]
  s.summary      = package["description"]
  s.author       = "Mads Lee Jensen <madsleejensen@gmail.com>"
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.platform     = :ios, "9.0"
  s.source       = {:git => "https://github.com/madsleejensen/react-native-image-sequence.git" }
  s.source_files  = "ios/RCTImageSequence/*.{h,m}"
  s.dependency "React"
end
