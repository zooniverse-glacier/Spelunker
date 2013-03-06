require 'uri'
require 'sinatra/base'
require 'redis'
require 'json'
require 'redis/connection/synchrony'

module Sinatra
  module RedisCache
    module Helpers
      def cache key, &block
        cached = settings.redis.get key
        if cached.nil? and block_given?
          data = block.call
          settings.redis.set key, data.to_json
          data
        else
          JSON.parse(cached)
        end
      end
    end

    def redis= url
      @redis = nil
      set :redis_url, url
      redis
    end

    def redis
      @redis ||= EventMachine::Synchrony::ConnectionPool.new(size: redis_pool_size) do
        Redis.new driver: :synchrony, url: redis_url
      end
    end

    def self.registered app
      app.helpers RedisCache::Helpers
      
      app.set :redis_url, 'http://127.0.0.1:6379'
      app.set :redis_pool_size, 2

      app.redis

    end
  end

  register RedisCache
end