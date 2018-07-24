require 'json'
version = JSON.parse(File.read('package.json'))["version"]

Pod::Spec.new do |s|

  s.name            = "RCTImageSequence"
  s.version         = version
  s.homepage        = "https://github.com/madsleejensen/react-native-image-sequence"
  s.summary         = "A <ImageSequence> component for react-native"
  s.license         = "MIT"
  s.author          = { "Mads Lee Jensen" => "madsleejensen@gmail.com" }
  s.ios.deployment_target = '7.0'
  s.source          = { :git => "https://github.com/madsleejensen/react-native-image-sequence.git", :tag => "#{s.version}" }
  s.source_files    = 'ios/**/*.{h,m}'
  s.preserve_paths  = "**/*.js"
  
  s.dependency 'React'

end
