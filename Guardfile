# A sample Guardfile
# More info at https://github.com/guard/guard#readme

guard 'rspec' do
  watch(%r{^spec/.+_spec\.rb$})
  watch('spec/spec_helper.rb')  { "spec" }
  watch(%r{^data_sources/(.+).rb$})  { |m| "spec/data_sources/#{m[1]}_spec.rb" }
  watch('spelunker.rb') { "spec/spelunker_spec.rb" }
end

